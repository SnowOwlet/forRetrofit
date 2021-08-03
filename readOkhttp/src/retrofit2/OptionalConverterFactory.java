package retrofit2;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import javax.annotation.Nullable;
import okhttp3.ResponseBody;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;

@IgnoreJRERequirement
final class OptionalConverterFactory extends Converter.Factory {
  static final Converter.Factory INSTANCE = new OptionalConverterFactory();
  
  @Nullable
  public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
    if (getRawType(type) != Optional.class)
      return null; 
    Type innerType = getParameterUpperBound(0, (ParameterizedType)type);
    Converter<ResponseBody, Object> delegate = retrofit.responseBodyConverter(innerType, annotations);
    return new OptionalConverter(delegate);
  }
  
  @IgnoreJRERequirement
  static final class OptionalConverter<T> implements Converter<ResponseBody, Optional<T>> {
    final Converter<ResponseBody, T> delegate;
    
    OptionalConverter(Converter<ResponseBody, T> delegate) {
      this.delegate = delegate;
    }
    
    public Optional<T> convert(ResponseBody value) throws IOException {
      return Optional.ofNullable(this.delegate.convert(value));
    }
  }
}
