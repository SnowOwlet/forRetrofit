package retrofit2;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Invocation {
  private final Method method;
  
  private final List<?> arguments;
  
  public static Invocation of(Method method, List<?> arguments) {
    Objects.requireNonNull(method, "method == null");
    Objects.requireNonNull(arguments, "arguments == null");
    return new Invocation(method, new ArrayList(arguments));
  }
  
  Invocation(Method method, List<?> arguments) {
    this.method = method;
    this.arguments = Collections.unmodifiableList(arguments);
  }
  
  public Method method() {
    return this.method;
  }
  
  public List<?> arguments() {
    return this.arguments;
  }
  
  public String toString() {
    return String.format("%s.%s() %s", new Object[] { this.method
          .getDeclaringClass().getName(), this.method.getName(), this.arguments });
  }
}
