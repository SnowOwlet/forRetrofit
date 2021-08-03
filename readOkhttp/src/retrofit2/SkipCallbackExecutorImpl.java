package retrofit2;

import java.lang.annotation.Annotation;

final class SkipCallbackExecutorImpl implements SkipCallbackExecutor {
  private static final SkipCallbackExecutor INSTANCE = new SkipCallbackExecutorImpl();
  
  static Annotation[] ensurePresent(Annotation[] annotations) {
    if (Utils.isAnnotationPresent(annotations, (Class)SkipCallbackExecutor.class))
      return annotations; 
    Annotation[] newAnnotations = new Annotation[annotations.length + 1];
    newAnnotations[0] = INSTANCE;
    System.arraycopy(annotations, 0, newAnnotations, 1, annotations.length);
    return newAnnotations;
  }
  
  public Class<? extends Annotation> annotationType() {
    return (Class)SkipCallbackExecutor.class;
  }
  
  public boolean equals(Object obj) {
    return obj instanceof SkipCallbackExecutor;
  }
  
  public int hashCode() {
    return 0;
  }
  
  public String toString() {
    return "@" + SkipCallbackExecutor.class.getName() + "()";
  }
}
