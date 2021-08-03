package retrofit2;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.annotation.Nullable;
import kotlin.coroutines.Continuation;
import okhttp3.Call;
import okhttp3.Response;
import okhttp3.ResponseBody;

abstract class HttpServiceMethod<ResponseT, ReturnT> extends ServiceMethod<ReturnT> {
  private final RequestFactory requestFactory;
  
  private final Call.Factory callFactory;
  
  private final Converter<ResponseBody, ResponseT> responseConverter;
  
  static <ResponseT, ReturnT> HttpServiceMethod<ResponseT, ReturnT> parseAnnotations(Retrofit retrofit, Method method, RequestFactory requestFactory) {
    Type adapterType;
    boolean isKotlinSuspendFunction = requestFactory.isKotlinSuspendFunction;
    boolean continuationWantsResponse = false;
    boolean continuationBodyNullable = false;
    Annotation[] annotations = method.getAnnotations();//这里拿到了method里的注解信息
    if (isKotlinSuspendFunction) {//这里是kotlin相关的一些方法
      Type[] parameterTypes = method.getGenericParameterTypes();
      Type type = Utils.getParameterLowerBound(0, (ParameterizedType)parameterTypes[parameterTypes.length - 1]);
      if (Utils.getRawType(type) == Response.class && type instanceof ParameterizedType) {
        type = Utils.getParameterUpperBound(0, (ParameterizedType)type);
        continuationWantsResponse = true;
      } 
      adapterType = new Utils.ParameterizedTypeImpl(null, Call.class, new Type[] { type });
      annotations = SkipCallbackExecutorImpl.ensurePresent(annotations);
    } else {
      adapterType = method.getGenericReturnType();//这里拿到类型
    }
    // 返回一个CallAdapted，而这个CallAdapted传入了我们刚才创建的callAdapter
    CallAdapter<ResponseT, ReturnT> callAdapter = createCallAdapter(retrofit, method, adapterType, annotations);//这里最终会得到我们在构建Retrofit调用CallAdapterFactories添加的对象的get方法
    Type responseType = callAdapter.responseType();
    if (responseType == Response.class)
      throw Utils.methodError(method, "'" + 
          
          Utils.getRawType(responseType).getName() + "' is not a valid response body type. Did you mean ResponseBody?", new Object[0]); 
    if (responseType == Response.class)
      throw Utils.methodError(method, "Response must include generic type (e.g., Response<String>)", new Object[0]); 
    if (requestFactory.httpMethod.equals("HEAD") && !Void.class.equals(responseType))
      throw Utils.methodError(method, "HEAD method must use Void as response type.", new Object[0]); 
    Converter<ResponseBody, ResponseT> responseConverter = createResponseConverter(retrofit, method, responseType);//这里也是拿到某不为人知的Converter(
    Call.Factory callFactory = retrofit.callFactory;
    if (!isKotlinSuspendFunction)
      return new CallAdapted<>(requestFactory, callFactory, responseConverter, callAdapter);//这里就返回一个CallAdapted:HttpServiceMethod
    if (continuationWantsResponse)
      return (HttpServiceMethod)new SuspendForResponse<>(requestFactory, callFactory, responseConverter, (CallAdapter)callAdapter);
    return (HttpServiceMethod)new SuspendForBody<>(requestFactory, callFactory, responseConverter, (CallAdapter)callAdapter, continuationBodyNullable);
  }
  
  private static <ResponseT, ReturnT> CallAdapter<ResponseT, ReturnT> createCallAdapter(Retrofit retrofit, Method method, Type returnType, Annotation[] annotations) {
    try {
      return (CallAdapter)retrofit.callAdapter(returnType, annotations);
    } catch (RuntimeException e) {
      throw Utils.methodError(method, e, "Unable to create call adapter for %s", new Object[] { returnType });
    } 
  }
  
  private static <ResponseT> Converter<ResponseBody, ResponseT> createResponseConverter(Retrofit retrofit, Method method, Type responseType) {
    Annotation[] annotations = method.getAnnotations();
    try {
      return retrofit.responseBodyConverter(responseType, annotations);
    } catch (RuntimeException e) {
      throw Utils.methodError(method, e, "Unable to create converter for %s", new Object[] { responseType });
    } 
  }
  
  HttpServiceMethod(RequestFactory requestFactory, Call.Factory callFactory, Converter<ResponseBody, ResponseT> responseConverter) {
    this.requestFactory = requestFactory;
    this.callFactory = callFactory;
    this.responseConverter = responseConverter;
  }
  
  @Nullable
  final ReturnT invoke(Object[] args) {
    // 创建OkHttpCall，继承了Call
    Call<ResponseT> call = new OkHttpCall<>(this.requestFactory, args, this.callFactory, this.responseConverter);
    // 调用了下面的adapt方法，实际是由CallAdapted来实现了这个方法并被调用
    return adapt(call, args);
  }
  // 找到实现了这个方法的类CallAdapted
  @Nullable
  protected abstract ReturnT adapt(Call<ResponseT> paramCall, Object[] paramArrayOfObject);
  
  static final class CallAdapted<ResponseT, ReturnT> extends HttpServiceMethod<ResponseT, ReturnT> {
    private final CallAdapter<ResponseT, ReturnT> callAdapter;
    // 由构造方法传入了我们的CallAdapter
    CallAdapted(RequestFactory requestFactory, Call.Factory callFactory, Converter<ResponseBody, ResponseT> responseConverter, CallAdapter<ResponseT, ReturnT> callAdapter) {
      super(requestFactory, callFactory, responseConverter);
      this.callAdapter = callAdapter;
    }
    //实际上是调用了callAdapter.adapt(call)
    protected ReturnT adapt(Call<ResponseT> call, Object[] args) {
      return this.callAdapter.adapt(call);
    }
  }
  
  static final class SuspendForResponse<ResponseT> extends HttpServiceMethod<ResponseT, Object> {
    private final CallAdapter<ResponseT, Call<ResponseT>> callAdapter;
    
    SuspendForResponse(RequestFactory requestFactory, Call.Factory callFactory, Converter<ResponseBody, ResponseT> responseConverter, CallAdapter<ResponseT, Call<ResponseT>> callAdapter) {
      super(requestFactory, callFactory, responseConverter);
      this.callAdapter = callAdapter;
    }
    
    protected Object adapt(Call<ResponseT> call, Object[] args) {
      call = this.callAdapter.adapt(call);
      Continuation<Response<ResponseT>> continuation = (Continuation<Response<ResponseT>>)args[args.length - 1];
      try {
        return KotlinExtensions.awaitResponse(call, continuation);
      } catch (Exception e) {
        return KotlinExtensions.suspendAndThrow(e, continuation);
      } 
    }
  }
  
  static final class SuspendForBody<ResponseT> extends HttpServiceMethod<ResponseT, Object> {
    private final CallAdapter<ResponseT, Call<ResponseT>> callAdapter;
    
    private final boolean isNullable;
    
    SuspendForBody(RequestFactory requestFactory, Call.Factory callFactory, Converter<ResponseBody, ResponseT> responseConverter, CallAdapter<ResponseT, Call<ResponseT>> callAdapter, boolean isNullable) {
      super(requestFactory, callFactory, responseConverter);
      this.callAdapter = callAdapter;
      this.isNullable = isNullable;
    }
    
    protected Object adapt(Call<ResponseT> call, Object[] args) {
      call = this.callAdapter.adapt(call);
      Continuation<ResponseT> continuation = (Continuation<ResponseT>)args[args.length - 1];
      try {
        return this.isNullable ? 
          KotlinExtensions.<ResponseT>awaitNullable(call, continuation) : 
          KotlinExtensions.<ResponseT>await(call, continuation);
      } catch (Exception e) {
        return KotlinExtensions.suspendAndThrow(e, continuation);
      } 
    }
  }
}
