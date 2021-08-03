package retrofit2;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.annotation.Nullable;

public interface CallAdapter<R, T> {
  Type responseType();
  
  T adapt(Call<R> paramCall);//这里的接口就是
  
  public static abstract class Factory {
    @Nullable
    public abstract CallAdapter<?, ?> get(Type param1Type, Annotation[] param1ArrayOfAnnotation, Retrofit param1Retrofit);
    
    protected static Type getParameterUpperBound(int index, ParameterizedType type) {
      return Utils.getParameterUpperBound(index, type);
    }
    
    protected static Class<?> getRawType(Type type) {
      return Utils.getRawType(type);
    }
  }
}
