package retrofit2;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;

class Platform {
  private static final Platform PLATFORM = findPlatform();
  
  private final boolean hasJava8Types;
  
  @Nullable
  private final Constructor<MethodHandles.Lookup> lookupConstructor;
  
  static Platform get() {
    return PLATFORM;
  }
  
  private static Platform findPlatform() {
    return "Dalvik".equals(System.getProperty("java.vm.name")) ?  //这里是判断是否为Android平台
      new Android() : 
      new Platform(true);
  }
  
  Platform(boolean hasJava8Types) {
    this.hasJava8Types = hasJava8Types;
    Constructor<MethodHandles.Lookup> lookupConstructor = null;
    if (hasJava8Types)
      try {
        lookupConstructor = MethodHandles.Lookup.class.getDeclaredConstructor(new Class[] { Class.class, int.class });
        lookupConstructor.setAccessible(true);
      } catch (NoClassDefFoundError noClassDefFoundError) {
      
      } catch (NoSuchMethodException noSuchMethodException) {} 
    this.lookupConstructor = lookupConstructor;
  }
  
  @Nullable
  Executor defaultCallbackExecutor() {
    return null;
  }
  
  List<? extends CallAdapter.Factory> defaultCallAdapterFactories(@Nullable Executor callbackExecutor) {
    DefaultCallAdapterFactory executorFactory = new DefaultCallAdapterFactory(callbackExecutor);
    return this.hasJava8Types ? 
      Arrays.<CallAdapter.Factory>asList(new CallAdapter.Factory[] { CompletableFutureCallAdapterFactory.INSTANCE, executorFactory }) : Collections.<CallAdapter.Factory>singletonList(executorFactory);
  }
  
  int defaultCallAdapterFactoriesSize() {
    return this.hasJava8Types ? 2 : 1;
  }
  
  List<? extends Converter.Factory> defaultConverterFactories() {
    return this.hasJava8Types ? Collections.<Converter.Factory>singletonList(OptionalConverterFactory.INSTANCE) : Collections.<Converter.Factory>emptyList();
  }
  
  int defaultConverterFactoriesSize() {
    return this.hasJava8Types ? 1 : 0;
  }
  
  @IgnoreJRERequirement
  boolean isDefaultMethod(Method method) {
    return (this.hasJava8Types && method.isDefault());
  }
  
  @Nullable
  @IgnoreJRERequirement
  Object invokeDefaultMethod(Method method, Class<?> declaringClass, Object object, Object... args) throws Throwable {
    MethodHandles.Lookup lookup = (this.lookupConstructor != null) ? this.lookupConstructor.newInstance(new Object[] { declaringClass, Integer.valueOf(-1) }) : MethodHandles.lookup();
    return lookup.unreflectSpecial(method, declaringClass).bindTo(object).invokeWithArguments(args);
  }
  
  static final class Android extends Platform {
    Android() {
      super((Build.VERSION.SDK_INT >= 24));
    }
    
    public Executor defaultCallbackExecutor() {
      return new MainThreadExecutor();
    }
    
    @Nullable
    Object invokeDefaultMethod(Method method, Class<?> declaringClass, Object object, Object... args) throws Throwable {
      if (Build.VERSION.SDK_INT < 26)
        throw new UnsupportedOperationException("Calling default methods on API 24 and 25 is not supported"); 
      return super.invokeDefaultMethod(method, declaringClass, object, args);
    }
    
    static final class MainThreadExecutor implements Executor {
      private final Handler handler = new Handler(Looper.getMainLooper());  //真正的目的是获得这个
      
      public void execute(Runnable r) {
        this.handler.post(r);
      }
    }
  }
}
