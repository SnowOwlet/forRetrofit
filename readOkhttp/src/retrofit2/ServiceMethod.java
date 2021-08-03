package retrofit2;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import javax.annotation.Nullable;

abstract class ServiceMethod<T> {
  static <T> ServiceMethod<T> parseAnnotations(Retrofit retrofit, Method method) {
    RequestFactory requestFactory = RequestFactory.parseAnnotations(retrofit, method);// 获得RequestFactory对象：对注解进行解析获得请求的参数、方法及url等信息，最终交给OkHttpCall来构建Call
    Type returnType = method.getGenericReturnType();
    if (Utils.hasUnresolvableType(returnType))
      throw Utils.methodError(method, "Method return type must not include a type variable or wildcard: %s", new Object[] { returnType }); 
    if (returnType == void.class)
      throw Utils.methodError(method, "Service methods cannot return void.", new Object[0]); 
    return HttpServiceMethod.parseAnnotations(retrofit, method, requestFactory);//最后调用了HttpServiceMethod
  }
  
  @Nullable
  abstract T invoke(Object[] paramArrayOfObject);
}
