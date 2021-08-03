package retrofit2;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import okhttp3.Request;
import okio.Timeout;

final class DefaultCallAdapterFactory extends CallAdapter.Factory {
  @Nullable
  private final Executor callbackExecutor;
  
  DefaultCallAdapterFactory(@Nullable Executor callbackExecutor) {
    this.callbackExecutor = callbackExecutor;
  }
  
  @Nullable
  public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
    if (getRawType(returnType) != Call.class)
      return null; 
    if (!(returnType instanceof ParameterizedType))
      throw new IllegalArgumentException("Call return type must be parameterized as Call<Foo> or Call<? extends Foo>"); 
    final Type responseType = Utils.getParameterUpperBound(0, (ParameterizedType)returnType);
    final Executor executor = Utils.isAnnotationPresent(annotations, (Class)SkipCallbackExecutor.class) ? null : this.callbackExecutor;

    return new CallAdapter<Object, Call<?>>() {//这里的返回值
        public Type responseType() {
          return responseType;
        }
        
        public Call<Object> adapt(Call<Object> call) {//和CallAdapter接口对应
          return (executor == null) ? call : new DefaultCallAdapterFactory.ExecutorCallbackCall(executor, call);//这个返回值，最后到了下面的Call<T>的代理，把所有的操作都进行了
        }
      };
  }
  
  static final class ExecutorCallbackCall<T> implements Call<T> {//这里是使用了代理
    final Executor callbackExecutor;
    
    final Call<T> delegate;
    
    ExecutorCallbackCall(Executor callbackExecutor, Call<T> delegate) {
      this.callbackExecutor = callbackExecutor;
      this.delegate = delegate;
    }
    
    public void enqueue(final Callback<T> callback) {
      Objects.requireNonNull(callback, "callback == null");
      this.delegate.enqueue(new Callback<T>() {
            public void onResponse(Call<T> call, Response<T> response) {
              DefaultCallAdapterFactory.ExecutorCallbackCall.this.callbackExecutor.execute(() -> {//callbackExecutor
                    if (DefaultCallAdapterFactory.ExecutorCallbackCall.this.delegate.isCanceled()) {
                      callback.onFailure(DefaultCallAdapterFactory.ExecutorCallbackCall.this, new IOException("Canceled"));//这里就是回调的地方
                    } else {
                      callback.onResponse(DefaultCallAdapterFactory.ExecutorCallbackCall.this, response);
                    } 
                  });
            }
            
            public void onFailure(Call<T> call, Throwable t) {
              DefaultCallAdapterFactory.ExecutorCallbackCall.this.callbackExecutor.execute(() -> callback.onFailure(DefaultCallAdapterFactory.ExecutorCallbackCall.this, t));
            }
          });
    }
    
    public boolean isExecuted() {
      return this.delegate.isExecuted();
    }
    
    public Response<T> execute() throws IOException {
      return this.delegate.execute();
    }
    
    public void cancel() {
      this.delegate.cancel();
    }
    
    public boolean isCanceled() {
      return this.delegate.isCanceled();
    }
    
    public Call<T> clone() {
      return new ExecutorCallbackCall(this.callbackExecutor, this.delegate.clone());
    }
    
    public Request request() {
      return this.delegate.request();
    }
    
    public Timeout timeout() {
      return this.delegate.timeout();
    }
  }
}
