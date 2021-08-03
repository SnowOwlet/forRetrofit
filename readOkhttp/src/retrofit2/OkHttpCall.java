package retrofit2;

import java.io.IOException;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;
import okio.Timeout;

final class OkHttpCall<T> implements Call<T> {
  private final RequestFactory requestFactory;
  
  private final Object[] args;
  
  private final Call.Factory callFactory;
  
  private final Converter<ResponseBody, T> responseConverter;
  
  private volatile boolean canceled;
  
  @Nullable
  @GuardedBy("this")
  private Call rawCall;
  
  @Nullable
  @GuardedBy("this")
  private Throwable creationFailure;
  
  @GuardedBy("this")
  private boolean executed;
  
  OkHttpCall(RequestFactory requestFactory, Object[] args, Call.Factory callFactory, Converter<ResponseBody, T> responseConverter) {
    this.requestFactory = requestFactory;
    this.args = args;
    this.callFactory = callFactory;
    this.responseConverter = responseConverter;
  }
  
  public OkHttpCall<T> clone() {
    return new OkHttpCall(this.requestFactory, this.args, this.callFactory, this.responseConverter);
  }
  
  public synchronized Request request() {
    try {
      return getRawCall().request();
    } catch (IOException e) {
      throw new RuntimeException("Unable to create request.", e);
    } 
  }
  
  public synchronized Timeout timeout() {
    try {
      return getRawCall().timeout();
    } catch (IOException e) {
      throw new RuntimeException("Unable to create call.", e);
    } 
  }
  
  @GuardedBy("this")
  private Call getRawCall() throws IOException {
    Call call = this.rawCall;
    if (call != null)
      return call; 
    if (this.creationFailure != null) {
      if (this.creationFailure instanceof IOException)
        throw (IOException)this.creationFailure; 
      if (this.creationFailure instanceof RuntimeException)
        throw (RuntimeException)this.creationFailure; 
      throw (Error)this.creationFailure;
    } 
    try {
      return this.rawCall = createRawCall();
    } catch (RuntimeException|Error|IOException e) {
      Utils.throwIfFatal(e);
      this.creationFailure = e;
      throw e;
    } 
  }
  
  public void enqueue(final Callback<T> callback) {
    Call call;
    Throwable failure;
    Objects.requireNonNull(callback, "callback == null");
    synchronized (this) {
      if (this.executed)
        throw new IllegalStateException("Already executed."); 
      this.executed = true;
      call = this.rawCall;
      failure = this.creationFailure;
      if (call == null && failure == null)
        try {
          call = this.rawCall = createRawCall();
        } catch (Throwable t) {
          Utils.throwIfFatal(t);
          failure = this.creationFailure = t;
        }  
    } 
    if (failure != null) {
      callback.onFailure(this, failure);
      return;
    } 
    if (this.canceled)
      call.cancel(); 
    call.enqueue(new Callback() {
          public void onResponse(Call call, Response rawResponse) {
            Response<T> response;
            try {
              response = OkHttpCall.this.parseResponse(rawResponse);
            } catch (Throwable e) {
              Utils.throwIfFatal(e);
              callFailure(e);
              return;
            } 
            try {
              callback.onResponse(OkHttpCall.this, response);
            } catch (Throwable t) {
              Utils.throwIfFatal(t);
              t.printStackTrace();
            } 
          }
          
          public void onFailure(Call call, IOException e) {
            callFailure(e);
          }
          
          private void callFailure(Throwable e) {
            try {
              callback.onFailure(OkHttpCall.this, e);
            } catch (Throwable t) {
              Utils.throwIfFatal(t);
              t.printStackTrace();
            } 
          }
        });
  }
  
  public synchronized boolean isExecuted() {
    return this.executed;
  }
  
  public Response<T> execute() throws IOException {
    Call call;
    synchronized (this) {
      if (this.executed)
        throw new IllegalStateException("Already executed."); 
      this.executed = true;
      call = getRawCall();
    } 
    if (this.canceled)
      call.cancel(); 
    return parseResponse(call.execute());
  }
  
  private Call createRawCall() throws IOException {
    // CallFactory是okhttp3.Call.Factory
    // 在Retrofit创建过程中就已经创建或设置了callFactory，这个callFactory就是传入的或者创建的OkHttpClient
    // requestFactory.create(args)返回okhttp3.Request
    // RequestFactory是对代理接口的方法及其注解进行解析的工厂类，获得对应的参数、请求method、url等信息构建okhttp3.Request
    Call call = this.callFactory.newCall(this.requestFactory.create(this.args));
    if (call == null)
      throw new NullPointerException("Call.Factory returned null.");
    // 最终返回的始终是一个okhttp3.Call
    return call;
  }
  
  Response<T> parseResponse(Response rawResponse) throws IOException {
    ResponseBody rawBody = rawResponse.body();
    rawResponse = rawResponse.newBuilder().body(new NoContentResponseBody(rawBody.contentType(), rawBody.contentLength())).build();
    int code = rawResponse.code();
    if (code < 200 || code >= 300)
      try {
        ResponseBody bufferedBody = Utils.buffer(rawBody);
        return (Response)Response.error(bufferedBody, rawResponse);
      } finally {
        rawBody.close();
      }  
    if (code == 204 || code == 205) {
      rawBody.close();
      return Response.success((T)null, rawResponse);
    } 
    ExceptionCatchingResponseBody catchingBody = new ExceptionCatchingResponseBody(rawBody);
    try {
      // 最终在这里通过Converter来实现返回数据的类型转化
      T body = this.responseConverter.convert(catchingBody);
      // 最终封装成Retrofit的Response返回
      return Response.success(body, rawResponse);
    } catch (RuntimeException e) {
      catchingBody.throwIfCaught();
      throw e;
    } 
  }
  
  public void cancel() {
    Call call;
    this.canceled = true;
    synchronized (this) {
      call = this.rawCall;
    } 
    if (call != null)
      call.cancel(); 
  }
  
  public boolean isCanceled() {
    if (this.canceled)
      return true; 
    synchronized (this) {
      return (this.rawCall != null && this.rawCall.isCanceled());
    } 
  }
  
  static final class NoContentResponseBody extends ResponseBody {
    @Nullable
    private final MediaType contentType;
    
    private final long contentLength;
    
    NoContentResponseBody(@Nullable MediaType contentType, long contentLength) {
      this.contentType = contentType;
      this.contentLength = contentLength;
    }
    
    public MediaType contentType() {
      return this.contentType;
    }
    
    public long contentLength() {
      return this.contentLength;
    }
    
    public BufferedSource source() {
      throw new IllegalStateException("Cannot read raw response body of a converted body.");
    }
  }
  
  static final class ExceptionCatchingResponseBody extends ResponseBody {
    private final ResponseBody delegate;
    
    private final BufferedSource delegateSource;
    
    @Nullable
    IOException thrownException;
    
    ExceptionCatchingResponseBody(ResponseBody delegate) {
      this.delegate = delegate;
      this
        .delegateSource = Okio.buffer((Source)new ForwardingSource((Source)delegate
            .source()) {
            public long read(Buffer sink, long byteCount) throws IOException {
              try {
                return super.read(sink, byteCount);
              } catch (IOException e) {
                OkHttpCall.ExceptionCatchingResponseBody.this.thrownException = e;
                throw e;
              } 
            }
          });
    }
    
    public MediaType contentType() {
      return this.delegate.contentType();
    }
    
    public long contentLength() {
      return this.delegate.contentLength();
    }
    
    public BufferedSource source() {
      return this.delegateSource;
    }
    
    public void close() {
      this.delegate.close();
    }
    
    void throwIfCaught() throws IOException {
      if (this.thrownException != null)
        throw this.thrownException; 
    }
  }
}
