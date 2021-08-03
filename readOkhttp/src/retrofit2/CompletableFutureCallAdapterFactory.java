package retrofit2;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;

@IgnoreJRERequirement
final class CompletableFutureCallAdapterFactory extends CallAdapter.Factory {
  static final CallAdapter.Factory INSTANCE = new CompletableFutureCallAdapterFactory();
  
  @Nullable
  public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
    if (getRawType(returnType) != CompletableFuture.class)
      return null; 
    if (!(returnType instanceof ParameterizedType))
      throw new IllegalStateException("CompletableFuture return type must be parameterized as CompletableFuture<Foo> or CompletableFuture<? extends Foo>"); 
    Type innerType = getParameterUpperBound(0, (ParameterizedType)returnType);
    if (getRawType(innerType) != Response.class)
      return new BodyCallAdapter(innerType); 
    if (!(innerType instanceof ParameterizedType))
      throw new IllegalStateException("Response must be parameterized as Response<Foo> or Response<? extends Foo>"); 
    Type responseType = getParameterUpperBound(0, (ParameterizedType)innerType);
    return new ResponseCallAdapter(responseType);
  }
  
  @IgnoreJRERequirement
  private static final class BodyCallAdapter<R> implements CallAdapter<R, CompletableFuture<R>> {
    private final Type responseType;
    
    BodyCallAdapter(Type responseType) {
      this.responseType = responseType;
    }
    
    public Type responseType() {
      return this.responseType;
    }
    
    public CompletableFuture<R> adapt(Call<R> call) {
      CompletableFuture<R> future = new CompletableFutureCallAdapterFactory.CallCancelCompletableFuture<>(call);
      call.enqueue(new BodyCallback(future));
      return future;
    }
    
    @IgnoreJRERequirement
    private class BodyCallback implements Callback<R> {
      private final CompletableFuture<R> future;
      
      public BodyCallback(CompletableFuture<R> future) {
        this.future = future;
      }
      
      public void onResponse(Call<R> call, Response<R> response) {
        if (response.isSuccessful()) {
          this.future.complete(response.body());
        } else {
          this.future.completeExceptionally(new HttpException(response));
        } 
      }
      
      public void onFailure(Call<R> call, Throwable t) {
        this.future.completeExceptionally(t);
      }
    }
  }
  
  @IgnoreJRERequirement
  private static final class ResponseCallAdapter<R> implements CallAdapter<R, CompletableFuture<Response<R>>> {
    private final Type responseType;
    
    ResponseCallAdapter(Type responseType) {
      this.responseType = responseType;
    }
    
    public Type responseType() {
      return this.responseType;
    }
    
    public CompletableFuture<Response<R>> adapt(Call<R> call) {
      CompletableFuture<Response<R>> future = new CompletableFutureCallAdapterFactory.CallCancelCompletableFuture<>(call);
      call.enqueue(new ResponseCallback(future));
      return future;
    }
    
    @IgnoreJRERequirement
    private class ResponseCallback implements Callback<R> {
      private final CompletableFuture<Response<R>> future;
      
      public ResponseCallback(CompletableFuture<Response<R>> future) {
        this.future = future;
      }
      
      public void onResponse(Call<R> call, Response<R> response) {
        this.future.complete(response);
      }
      
      public void onFailure(Call<R> call, Throwable t) {
        this.future.completeExceptionally(t);
      }
    }
  }
  
  @IgnoreJRERequirement
  private static final class CallCancelCompletableFuture<T> extends CompletableFuture<T> {
    private final Call<?> call;
    
    CallCancelCompletableFuture(Call<?> call) {
      this.call = call;
    }
    
    public boolean cancel(boolean mayInterruptIfRunning) {
      if (mayInterruptIfRunning)
        this.call.cancel(); 
      return super.cancel(mayInterruptIfRunning);
    }
  }
}
