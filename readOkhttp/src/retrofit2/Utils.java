package retrofit2;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import javax.annotation.Nullable;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.Sink;

final class Utils {
  static final Type[] EMPTY_TYPE_ARRAY = new Type[0];
  
  static RuntimeException methodError(Method method, String message, Object... args) {
    return methodError(method, null, message, args);
  }
  
  static RuntimeException methodError(Method method, @Nullable Throwable cause, String message, Object... args) {
    message = String.format(message, args);
    return new IllegalArgumentException(message + "\n    for method " + method
        
        .getDeclaringClass().getSimpleName() + "." + method
        
        .getName(), cause);
  }
  
  static RuntimeException parameterError(Method method, Throwable cause, int p, String message, Object... args) {
    return methodError(method, cause, message + " (parameter #" + (p + 1) + ")", args);
  }
  
  static RuntimeException parameterError(Method method, int p, String message, Object... args) {
    return methodError(method, message + " (parameter #" + (p + 1) + ")", args);
  }
  
  static Class<?> getRawType(Type type) {
    Objects.requireNonNull(type, "type == null");
    if (type instanceof Class)
      return (Class)type; 
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType)type;
      Type rawType = parameterizedType.getRawType();
      if (!(rawType instanceof Class))
        throw new IllegalArgumentException(); 
      return (Class)rawType;
    } 
    if (type instanceof GenericArrayType) {
      Type componentType = ((GenericArrayType)type).getGenericComponentType();
      return Array.newInstance(getRawType(componentType), 0).getClass();
    } 
    if (type instanceof TypeVariable)
      return Object.class; 
    if (type instanceof WildcardType)
      return getRawType(((WildcardType)type).getUpperBounds()[0]); 
    throw new IllegalArgumentException("Expected a Class, ParameterizedType, or GenericArrayType, but <" + type + "> is of type " + type
        
        .getClass().getName());
  }
  
  static boolean equals(Type a, Type b) {
    if (a == b)
      return true; 
    if (a instanceof Class)
      return a.equals(b); 
    if (a instanceof ParameterizedType) {
      if (!(b instanceof ParameterizedType))
        return false; 
      ParameterizedType pa = (ParameterizedType)a;
      ParameterizedType pb = (ParameterizedType)b;
      Object ownerA = pa.getOwnerType();
      Object ownerB = pb.getOwnerType();
      return ((ownerA == ownerB || (ownerA != null && ownerA.equals(ownerB))) && pa
        .getRawType().equals(pb.getRawType()) && 
        Arrays.equals((Object[])pa.getActualTypeArguments(), (Object[])pb.getActualTypeArguments()));
    } 
    if (a instanceof GenericArrayType) {
      if (!(b instanceof GenericArrayType))
        return false; 
      GenericArrayType ga = (GenericArrayType)a;
      GenericArrayType gb = (GenericArrayType)b;
      return equals(ga.getGenericComponentType(), gb.getGenericComponentType());
    } 
    if (a instanceof WildcardType) {
      if (!(b instanceof WildcardType))
        return false; 
      WildcardType wa = (WildcardType)a;
      WildcardType wb = (WildcardType)b;
      return (Arrays.equals((Object[])wa.getUpperBounds(), (Object[])wb.getUpperBounds()) && 
        Arrays.equals((Object[])wa.getLowerBounds(), (Object[])wb.getLowerBounds()));
    } 
    if (a instanceof TypeVariable) {
      if (!(b instanceof TypeVariable))
        return false; 
      TypeVariable<?> va = (TypeVariable)a;
      TypeVariable<?> vb = (TypeVariable)b;
      return (va.getGenericDeclaration() == vb.getGenericDeclaration() && va
        .getName().equals(vb.getName()));
    } 
    return false;
  }
  
  static Type getGenericSupertype(Type context, Class<?> rawType, Class<?> toResolve) {
    if (toResolve == rawType)
      return context; 
    if (toResolve.isInterface()) {
      Class<?>[] interfaces = rawType.getInterfaces();
      for (int i = 0, length = interfaces.length; i < length; i++) {
        if (interfaces[i] == toResolve)
          return rawType.getGenericInterfaces()[i]; 
        if (toResolve.isAssignableFrom(interfaces[i]))
          return getGenericSupertype(rawType.getGenericInterfaces()[i], interfaces[i], toResolve); 
      } 
    } 
    if (!rawType.isInterface())
      while (rawType != Object.class) {
        Class<?> rawSupertype = rawType.getSuperclass();
        if (rawSupertype == toResolve)
          return rawType.getGenericSuperclass(); 
        if (toResolve.isAssignableFrom(rawSupertype))
          return getGenericSupertype(rawType.getGenericSuperclass(), rawSupertype, toResolve); 
        rawType = rawSupertype;
      }  
    return toResolve;
  }
  
  private static int indexOf(Object[] array, Object toFind) {
    for (int i = 0; i < array.length; i++) {
      if (toFind.equals(array[i]))
        return i; 
    } 
    throw new NoSuchElementException();
  }
  
  static String typeToString(Type type) {
    return (type instanceof Class) ? ((Class)type).getName() : type.toString();
  }
  
  static Type getSupertype(Type context, Class<?> contextRawType, Class<?> supertype) {
    if (!supertype.isAssignableFrom(contextRawType))
      throw new IllegalArgumentException(); 
    return resolve(context, contextRawType, 
        getGenericSupertype(context, contextRawType, supertype));
  }
  
  static Type resolve(Type context, Class<?> contextRawType, Type toResolve) {
    while (toResolve instanceof TypeVariable) {
      TypeVariable<?> typeVariable = (TypeVariable)toResolve;
      toResolve = resolveTypeVariable(context, contextRawType, typeVariable);
      if (toResolve == typeVariable)
        return toResolve; 
    } 
    if (toResolve instanceof Class && ((Class)toResolve).isArray()) {
      Class<?> original = (Class)toResolve;
      Type<?> componentType = original.getComponentType();
      Type newComponentType = resolve(context, contextRawType, componentType);
      return (componentType == newComponentType) ? 
        original : 
        new GenericArrayTypeImpl(newComponentType);
    } 
    if (toResolve instanceof GenericArrayType) {
      GenericArrayType original = (GenericArrayType)toResolve;
      Type componentType = original.getGenericComponentType();
      Type newComponentType = resolve(context, contextRawType, componentType);
      return (componentType == newComponentType) ? 
        original : 
        new GenericArrayTypeImpl(newComponentType);
    } 
    if (toResolve instanceof ParameterizedType) {
      ParameterizedType original = (ParameterizedType)toResolve;
      Type ownerType = original.getOwnerType();
      Type newOwnerType = resolve(context, contextRawType, ownerType);
      boolean changed = (newOwnerType != ownerType);
      Type[] args = original.getActualTypeArguments();
      for (int t = 0, length = args.length; t < length; t++) {
        Type resolvedTypeArgument = resolve(context, contextRawType, args[t]);
        if (resolvedTypeArgument != args[t]) {
          if (!changed) {
            args = (Type[])args.clone();
            changed = true;
          } 
          args[t] = resolvedTypeArgument;
        } 
      } 
      return changed ? 
        new ParameterizedTypeImpl(newOwnerType, original.getRawType(), args) : 
        original;
    } 
    if (toResolve instanceof WildcardType) {
      WildcardType original = (WildcardType)toResolve;
      Type[] originalLowerBound = original.getLowerBounds();
      Type[] originalUpperBound = original.getUpperBounds();
      if (originalLowerBound.length == 1) {
        Type lowerBound = resolve(context, contextRawType, originalLowerBound[0]);
        if (lowerBound != originalLowerBound[0])
          return new WildcardTypeImpl(new Type[] { Object.class }, new Type[] { lowerBound }); 
      } else if (originalUpperBound.length == 1) {
        Type upperBound = resolve(context, contextRawType, originalUpperBound[0]);
        if (upperBound != originalUpperBound[0])
          return new WildcardTypeImpl(new Type[] { upperBound }, EMPTY_TYPE_ARRAY); 
      } 
      return original;
    } 
    return toResolve;
  }
  
  private static Type resolveTypeVariable(Type context, Class<?> contextRawType, TypeVariable<?> unknown) {
    Class<?> declaredByRaw = declaringClassOf(unknown);
    if (declaredByRaw == null)
      return unknown; 
    Type declaredBy = getGenericSupertype(context, contextRawType, declaredByRaw);
    if (declaredBy instanceof ParameterizedType) {
      int index = indexOf((Object[])declaredByRaw.getTypeParameters(), unknown);
      return ((ParameterizedType)declaredBy).getActualTypeArguments()[index];
    } 
    return unknown;
  }
  
  @Nullable
  private static Class<?> declaringClassOf(TypeVariable<?> typeVariable) {
    GenericDeclaration genericDeclaration = (GenericDeclaration)typeVariable.getGenericDeclaration();
    return (genericDeclaration instanceof Class) ? (Class)genericDeclaration : null;
  }
  
  static void checkNotPrimitive(Type type) {
    if (type instanceof Class && ((Class)type).isPrimitive())
      throw new IllegalArgumentException(); 
  }
  
  static boolean isAnnotationPresent(Annotation[] annotations, Class<? extends Annotation> cls) {
    for (Annotation annotation : annotations) {
      if (cls.isInstance(annotation))
        return true; 
    } 
    return false;
  }
  
  static ResponseBody buffer(ResponseBody body) throws IOException {
    Buffer buffer = new Buffer();
    body.source().readAll((Sink)buffer);
    return ResponseBody.create(body.contentType(), body.contentLength(), (BufferedSource)buffer);
  }
  
  static Type getParameterUpperBound(int index, ParameterizedType type) {
    Type[] types = type.getActualTypeArguments();
    if (index < 0 || index >= types.length)
      throw new IllegalArgumentException("Index " + index + " not in range [0," + types.length + ") for " + type); 
    Type paramType = types[index];
    if (paramType instanceof WildcardType)
      return ((WildcardType)paramType).getUpperBounds()[0]; 
    return paramType;
  }
  
  static Type getParameterLowerBound(int index, ParameterizedType type) {
    Type paramType = type.getActualTypeArguments()[index];
    if (paramType instanceof WildcardType)
      return ((WildcardType)paramType).getLowerBounds()[0]; 
    return paramType;
  }
  
  static boolean hasUnresolvableType(@Nullable Type type) {
    if (type instanceof Class)
      return false; 
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType)type;
      for (Type typeArgument : parameterizedType.getActualTypeArguments()) {
        if (hasUnresolvableType(typeArgument))
          return true; 
      } 
      return false;
    } 
    if (type instanceof GenericArrayType)
      return hasUnresolvableType(((GenericArrayType)type).getGenericComponentType()); 
    if (type instanceof TypeVariable)
      return true; 
    if (type instanceof WildcardType)
      return true; 
    String className = (type == null) ? "null" : type.getClass().getName();
    throw new IllegalArgumentException("Expected a Class, ParameterizedType, or GenericArrayType, but <" + type + "> is of type " + className);
  }
  
  static final class ParameterizedTypeImpl implements ParameterizedType {
    @Nullable
    private final Type ownerType;
    
    private final Type rawType;
    
    private final Type[] typeArguments;
    
    ParameterizedTypeImpl(@Nullable Type ownerType, Type rawType, Type... typeArguments) {
      if (rawType instanceof Class)
        if (((ownerType == null) ? true : false) != (
          (((Class)rawType).getEnclosingClass() == null) ? true : false))
          throw new IllegalArgumentException();  
      for (Type typeArgument : typeArguments) {
        Objects.requireNonNull(typeArgument, "typeArgument == null");
        Utils.checkNotPrimitive(typeArgument);
      } 
      this.ownerType = ownerType;
      this.rawType = rawType;
      this.typeArguments = (Type[])typeArguments.clone();
    }
    
    public Type[] getActualTypeArguments() {
      return (Type[])this.typeArguments.clone();
    }
    
    public Type getRawType() {
      return this.rawType;
    }
    
    @Nullable
    public Type getOwnerType() {
      return this.ownerType;
    }
    
    public boolean equals(Object other) {
      return (other instanceof ParameterizedType && Utils.equals(this, (ParameterizedType)other));
    }
    
    public int hashCode() {
      return Arrays.hashCode((Object[])this.typeArguments) ^ this.rawType
        .hashCode() ^ (
        (this.ownerType != null) ? this.ownerType.hashCode() : 0);
    }
    
    public String toString() {
      if (this.typeArguments.length == 0)
        return Utils.typeToString(this.rawType); 
      StringBuilder result = new StringBuilder(30 * (this.typeArguments.length + 1));
      result.append(Utils.typeToString(this.rawType));
      result.append("<").append(Utils.typeToString(this.typeArguments[0]));
      for (int i = 1; i < this.typeArguments.length; i++)
        result.append(", ").append(Utils.typeToString(this.typeArguments[i])); 
      return result.append(">").toString();
    }
  }
  
  private static final class GenericArrayTypeImpl implements GenericArrayType {
    private final Type componentType;
    
    GenericArrayTypeImpl(Type componentType) {
      this.componentType = componentType;
    }
    
    public Type getGenericComponentType() {
      return this.componentType;
    }
    
    public boolean equals(Object o) {
      return (o instanceof GenericArrayType && Utils.equals(this, (GenericArrayType)o));
    }
    
    public int hashCode() {
      return this.componentType.hashCode();
    }
    
    public String toString() {
      return Utils.typeToString(this.componentType) + "[]";
    }
  }
  
  private static final class WildcardTypeImpl implements WildcardType {
    private final Type upperBound;
    
    @Nullable
    private final Type lowerBound;
    
    WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
      if (lowerBounds.length > 1)
        throw new IllegalArgumentException(); 
      if (upperBounds.length != 1)
        throw new IllegalArgumentException(); 
      if (lowerBounds.length == 1) {
        if (lowerBounds[0] == null)
          throw new NullPointerException(); 
        Utils.checkNotPrimitive(lowerBounds[0]);
        if (upperBounds[0] != Object.class)
          throw new IllegalArgumentException(); 
        this.lowerBound = lowerBounds[0];
        this.upperBound = Object.class;
      } else {
        if (upperBounds[0] == null)
          throw new NullPointerException(); 
        Utils.checkNotPrimitive(upperBounds[0]);
        this.lowerBound = null;
        this.upperBound = upperBounds[0];
      } 
    }
    
    public Type[] getUpperBounds() {
      return new Type[] { this.upperBound };
    }
    
    public Type[] getLowerBounds() {
      (new Type[1])[0] = this.lowerBound;
      return (this.lowerBound != null) ? new Type[1] : Utils.EMPTY_TYPE_ARRAY;
    }
    
    public boolean equals(Object other) {
      return (other instanceof WildcardType && Utils.equals(this, (WildcardType)other));
    }
    
    public int hashCode() {
      return ((this.lowerBound != null) ? (31 + this.lowerBound.hashCode()) : 1) ^ 31 + this.upperBound.hashCode();
    }
    
    public String toString() {
      if (this.lowerBound != null)
        return "? super " + Utils.typeToString(this.lowerBound); 
      if (this.upperBound == Object.class)
        return "?"; 
      return "? extends " + Utils.typeToString(this.upperBound);
    }
  }
  
  static void throwIfFatal(Throwable t) {
    if (t instanceof VirtualMachineError)
      throw (VirtualMachineError)t; 
    if (t instanceof ThreadDeath)
      throw (ThreadDeath)t; 
    if (t instanceof LinkageError)
      throw (LinkageError)t; 
  }
}
