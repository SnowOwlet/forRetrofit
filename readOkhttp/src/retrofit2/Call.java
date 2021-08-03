package retrofit2;

import java.io.IOException;
import okhttp3.Request;
import okio.Timeout;

public interface Call<T> extends Cloneable {
  Response<T> execute() throws IOException;
  
  void enqueue(Callback<T> paramCallback);
  
  boolean isExecuted();
  
  void cancel();
  
  boolean isCanceled();
  
  Call<T> clone();
  
  Request request();
  
  Timeout timeout();
}
