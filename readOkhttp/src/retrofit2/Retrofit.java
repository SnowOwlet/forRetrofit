package retrofit2;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.Nullable;

public final class Retrofit {
  private final Map<Method, ServiceMethod<?>> serviceMethodCache = new ConcurrentHashMap<>();
  
  final Call.Factory callFactory;
  
  final HttpUrl baseUrl;
  
  final List<Converter.Factory> converterFactories;
  
  final List<CallAdapter.Factory> callAdapterFactories; //这个就是
  
  @Nullable
  final Executor callbackExecutor;
  
  final boolean validateEagerly;
  
  Retrofit(Call.Factory callFactory, HttpUrl baseUrl, List<Converter.Factory> converterFactories, List<CallAdapter.Factory> callAdapterFactories, @Nullable Executor callbackExecutor, boolean validateEagerly) {
    this.callFactory = callFactory;
    this.baseUrl = baseUrl;
    this.converterFactories = converterFactories;
    this.callAdapterFactories = callAdapterFactories;
    this.callbackExecutor = callbackExecutor;
    this.validateEagerly = validateEagerly;
  }
  
  public <T> T create(final Class<T> service) {
    validateServiceInterface(service);
    return 
      (T)Proxy.newProxyInstance(service
        .getClassLoader(), new Class[] { service }, new InvocationHandler() {
          private final Platform platform = Platform.get();
          
          private final Object[] emptyArgs = new Object[0];
          
          @Nullable
          public Object invoke(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
            if (method.getDeclaringClass() == Object.class)//这里是判断是否为Object的方法，例如hasCode还有toString什么的,这些不反射
              return method.invoke(this, args);
            args = (args != null) ? args : this.emptyArgs;
            return this.platform.isDefaultMethod(method) ? //判断是否为default方法
              this.platform.invokeDefaultMethod(method, service, proxy, args) : 
              Retrofit.this.loadServiceMethod(method).invoke(args);//显然是走这
          }
        });
  }
  
  private void validateServiceInterface(Class<?> service) {
    if (!service.isInterface())
      throw new IllegalArgumentException("API declarations must be interfaces."); 
    Deque<Class<?>> check = new ArrayDeque<>(1);
    check.add(service);
    while (!check.isEmpty()) {
      Class<?> candidate = check.removeFirst();
      if ((candidate.getTypeParameters()).length != 0) {
        StringBuilder message = (new StringBuilder("Type parameters are unsupported on ")).append(candidate.getName());
        if (candidate != service)
          message.append(" which is an interface of ").append(service.getName()); 
        throw new IllegalArgumentException(message.toString());
      } 
      Collections.addAll(check, candidate.getInterfaces());
    } 
    if (this.validateEagerly) {//validateEagerly，这里就是让方法理科生效
      Platform platform = Platform.get();
      for (Method method : service.getDeclaredMethods()) {
        if (!platform.isDefaultMethod(method) && !Modifier.isStatic(method.getModifiers()))
          loadServiceMethod(method); 
      } 
    } 
  }
  // HttpServiceMethod是ServiceMethod的具体实现类，所以最终调用的是HttpServiceMethod对象的invoke方法
  ServiceMethod<?> loadServiceMethod(Method method) {
    ServiceMethod<?> result = this.serviceMethodCache.get(method);//缓存当中去取，如果缓存当中没有就去拿
    if (result != null)
      return result; 
    synchronized (this.serviceMethodCache) {
      result = this.serviceMethodCache.get(method);
      if (result == null) {
        result = ServiceMethod.parseAnnotations(this, method);//这里是调用ServiceMethod里的RequestFactory的parseAnnotations
        this.serviceMethodCache.put(method, result);
      } 
    } 
    return result;
  }
  
  public Call.Factory callFactory() {
    return this.callFactory;
  }
  
  public HttpUrl baseUrl() {
    return this.baseUrl;
  }
  
  public List<CallAdapter.Factory> callAdapterFactories() {
    return this.callAdapterFactories;
  }
  
  public CallAdapter<?, ?> callAdapter(Type returnType, Annotation[] annotations) {
    return nextCallAdapter(null, returnType, annotations);
  }
  
  public CallAdapter<?, ?> nextCallAdapter(@Nullable CallAdapter.Factory skipPast, Type returnType, Annotation[] annotations) {
    Objects.requireNonNull(returnType, "returnType == null");
    Objects.requireNonNull(annotations, "annotations == null");
    int start = this.callAdapterFactories.indexOf(skipPast) + 1;
    for (int i = start, count = this.callAdapterFactories.size(); i < count; i++) {
      CallAdapter<?, ?> adapter = ((CallAdapter.Factory)this.callAdapterFactories.get(i)).get(returnType, annotations, this);//这里就是把factories的东西放Call，规定他
      if (adapter != null)
        return adapter;
    } 
    StringBuilder builder = (new StringBuilder("Could not locate call adapter for ")).append(returnType).append(".\n");
    if (skipPast != null) {
      builder.append("  Skipped:");
      for (int m = 0; m < start; m++)
        builder.append("\n   * ").append(((CallAdapter.Factory)this.callAdapterFactories.get(m)).getClass().getName()); 
      builder.append('\n');
    } 
    builder.append("  Tried:");
    for (int j = start, k = this.callAdapterFactories.size(); j < k; j++)
      builder.append("\n   * ").append(((CallAdapter.Factory)this.callAdapterFactories.get(j)).getClass().getName()); 
    throw new IllegalArgumentException(builder.toString());
  }
  
  public List<Converter.Factory> converterFactories() {
    return this.converterFactories;
  }
  
  public <T> Converter<T, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations) {
    return nextRequestBodyConverter(null, type, parameterAnnotations, methodAnnotations);
  }
  
  public <T> Converter<T, RequestBody> nextRequestBodyConverter(@Nullable Converter.Factory skipPast, Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations) {
    Objects.requireNonNull(type, "type == null");
    Objects.requireNonNull(parameterAnnotations, "parameterAnnotations == null");
    Objects.requireNonNull(methodAnnotations, "methodAnnotations == null");
    int start = this.converterFactories.indexOf(skipPast) + 1;
    for (int i = start, count = this.converterFactories.size(); i < count; i++) {
      Converter.Factory factory = this.converterFactories.get(i);
      Converter<?, RequestBody> converter = factory.requestBodyConverter(type, parameterAnnotations, methodAnnotations, this);
      if (converter != null)
        return (Converter)converter; 
    } 
    StringBuilder builder = (new StringBuilder("Could not locate RequestBody converter for ")).append(type).append(".\n");
    if (skipPast != null) {
      builder.append("  Skipped:");
      for (int m = 0; m < start; m++)
        builder.append("\n   * ").append(((Converter.Factory)this.converterFactories.get(m)).getClass().getName()); 
      builder.append('\n');
    } 
    builder.append("  Tried:");
    for (int j = start, k = this.converterFactories.size(); j < k; j++)
      builder.append("\n   * ").append(((Converter.Factory)this.converterFactories.get(j)).getClass().getName()); 
    throw new IllegalArgumentException(builder.toString());
  }
  
  public <T> Converter<ResponseBody, T> responseBodyConverter(Type type, Annotation[] annotations) {
    return nextResponseBodyConverter(null, type, annotations);
  }
  
  public <T> Converter<ResponseBody, T> nextResponseBodyConverter(@Nullable Converter.Factory skipPast, Type type, Annotation[] annotations) {
    Objects.requireNonNull(type, "type == null");
    Objects.requireNonNull(annotations, "annotations == null");
    int start = this.converterFactories.indexOf(skipPast) + 1;
    for (int i = start, count = this.converterFactories.size(); i < count; i++) {
      Converter<ResponseBody, ?> converter = ((Converter.Factory)this.converterFactories.get(i)).responseBodyConverter(type, annotations, this);
      if (converter != null)
        return (Converter)converter; 
    } 
    StringBuilder builder = (new StringBuilder("Could not locate ResponseBody converter for ")).append(type).append(".\n");
    if (skipPast != null) {
      builder.append("  Skipped:");
      for (int m = 0; m < start; m++)
        builder.append("\n   * ").append(((Converter.Factory)this.converterFactories.get(m)).getClass().getName()); 
      builder.append('\n');
    } 
    builder.append("  Tried:");
    for (int j = start, k = this.converterFactories.size(); j < k; j++)
      builder.append("\n   * ").append(((Converter.Factory)this.converterFactories.get(j)).getClass().getName()); 
    throw new IllegalArgumentException(builder.toString());
  }
  
  public <T> Converter<T, String> stringConverter(Type type, Annotation[] annotations) {
    Objects.requireNonNull(type, "type == null");
    Objects.requireNonNull(annotations, "annotations == null");
    for (int i = 0, count = this.converterFactories.size(); i < count; i++) {
      Converter<?, String> converter = ((Converter.Factory)this.converterFactories.get(i)).stringConverter(type, annotations, this);
      if (converter != null)
        return (Converter)converter; 
    } 
    return BuiltInConverters.ToStringConverter.INSTANCE;
  }
  
  @Nullable
  public Executor callbackExecutor() {
    return this.callbackExecutor;
  }
  
  public Builder newBuilder() {
    return new Builder(this);
  }
  
  public static final class Builder {
    private final Platform platform;
    
    @Nullable
    private Call.Factory callFactory;
    
    @Nullable
    private HttpUrl baseUrl;
    
    private final List<Converter.Factory> converterFactories = new ArrayList<>();
    
    private final List<CallAdapter.Factory> callAdapterFactories = new ArrayList<>();
    
    @Nullable
    private Executor callbackExecutor;
    
    private boolean validateEagerly;
    
    Builder(Platform platform) {
      this.platform = platform;
    }
    
    public Builder() {
      this(Platform.get());
    }
    
    Builder(Retrofit retrofit) {
      this.platform = Platform.get();
      this.callFactory = retrofit.callFactory;
      this.baseUrl = retrofit.baseUrl;
      int i = 1;
      int size = retrofit.converterFactories.size() - this.platform.defaultConverterFactoriesSize();
      for (; i < size; 
        i++)
        this.converterFactories.add(retrofit.converterFactories.get(i)); 
      i = 0;
      size = retrofit.callAdapterFactories.size() - this.platform.defaultCallAdapterFactoriesSize();
      for (; i < size; 
        i++)
        this.callAdapterFactories.add(retrofit.callAdapterFactories.get(i)); 
      this.callbackExecutor = retrofit.callbackExecutor;
      this.validateEagerly = retrofit.validateEagerly;
    }
    
    public Builder client(OkHttpClient client) {
      return callFactory((Call.Factory)Objects.requireNonNull(client, "client == null"));
    }
    
    public Builder callFactory(Call.Factory factory) {
      this.callFactory = Objects.<Call.Factory>requireNonNull(factory, "factory == null");
      return this;
    }
    
    public Builder baseUrl(URL baseUrl) {
      Objects.requireNonNull(baseUrl, "baseUrl == null");
      return baseUrl(HttpUrl.get(baseUrl.toString()));
    }
    
    public Builder baseUrl(String baseUrl) {
      Objects.requireNonNull(baseUrl, "baseUrl == null");
      return baseUrl(HttpUrl.get(baseUrl));
    }
    
    public Builder baseUrl(HttpUrl baseUrl) {
      Objects.requireNonNull(baseUrl, "baseUrl == null");
      List<String> pathSegments = baseUrl.pathSegments();
      if (!"".equals(pathSegments.get(pathSegments.size() - 1)))
        throw new IllegalArgumentException("baseUrl must end in /: " + baseUrl); 
      this.baseUrl = baseUrl;
      return this;
    }
    
    public Builder addConverterFactory(Converter.Factory factory) {
      this.converterFactories.add(Objects.<Converter.Factory>requireNonNull(factory, "factory == null"));
      return this;
    }
    
    public Builder addCallAdapterFactory(CallAdapter.Factory factory) {
      this.callAdapterFactories.add(Objects.<CallAdapter.Factory>requireNonNull(factory, "factory == null"));
      return this;
    }
    
    public Builder callbackExecutor(Executor executor) {
      this.callbackExecutor = Objects.<Executor>requireNonNull(executor, "executor == null");
      return this;
    }
    
    public List<CallAdapter.Factory> callAdapterFactories() {
      return this.callAdapterFactories;
    }
    
    public List<Converter.Factory> converterFactories() {
      return this.converterFactories;
    }
    
    public Builder validateEagerly(boolean validateEagerly) {
      this.validateEagerly = validateEagerly;
      return this;
    }
    
    public Retrofit build() {
      OkHttpClient okHttpClient;
      if (this.baseUrl == null)
        throw new IllegalStateException("Base URL required."); 
      Call.Factory callFactory = this.callFactory;
      if (callFactory == null)
        okHttpClient = new OkHttpClient(); 
      Executor callbackExecutor = this.callbackExecutor;
      if (callbackExecutor == null)
        callbackExecutor = this.platform.defaultCallbackExecutor(); 
      List<CallAdapter.Factory> callAdapterFactories = new ArrayList<>(this.callAdapterFactories);//对CallAdapter进行了初始化
      callAdapterFactories.addAll(this.platform.defaultCallAdapterFactories(callbackExecutor));
      List<Converter.Factory> converterFactories = new ArrayList<>(1 + this.converterFactories.size() + this.platform.defaultConverterFactoriesSize());
      converterFactories.add(new BuiltInConverters());
      converterFactories.addAll(this.converterFactories);
      converterFactories.addAll(this.platform.defaultConverterFactories());
      return new Retrofit((Call.Factory)okHttpClient, this.baseUrl, 
          
          Collections.unmodifiableList(converterFactories), 
          Collections.unmodifiableList(callAdapterFactories), callbackExecutor, this.validateEagerly);
    }
  }
}
