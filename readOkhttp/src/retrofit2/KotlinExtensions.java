package retrofit2;

import kotlin.Metadata;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.ContinuationImpl;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.DebugProbesKt;
import kotlin.jvm.JvmName;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CancellableContinuation;
import kotlinx.coroutines.CancellableContinuationImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 2, d1 = {"\000.\n\002\b\002\n\002\020\000\n\002\030\002\n\002\b\003\n\002\030\002\n\000\n\002\030\002\n\002\b\002\n\002\020\001\n\002\030\002\n\002\030\002\n\002\b\002\032%\020\000\032\002H\001\"\b\b\000\020\001*\0020\002*\b\022\004\022\002H\0010\003H\u0086@\u00F8\001\000\u00A2\006\002\020\004\032+\020\000\032\004\030\001H\001\"\b\b\000\020\001*\0020\002*\n\022\006\022\004\030\001H\0010\003H\u0087@\u00F8\001\000\u00A2\006\004\b\005\020\004\032'\020\006\032\b\022\004\022\002H\0010\007\"\004\b\000\020\001*\b\022\004\022\002H\0010\003H\u0086@\u00F8\001\000\u00A2\006\002\020\004\032\032\020\b\032\002H\001\"\006\b\000\020\001\030\001*\0020\tH\u0086\b\u00A2\006\002\020\n\032\031\020\013\032\0020\f*\0060\rj\002`\016H\u0080@\u00F8\001\000\u00A2\006\002\020\017\u0082\002\004\n\002\b\031\u00A8\006\020"}, d2 = {"await", "T", "", "Lretrofit2/Call;", "(Lretrofit2/Call;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "awaitNullable", "awaitResponse", "Lretrofit2/Response;", "create", "Lretrofit2/Retrofit;", "(Lretrofit2/Retrofit;)Ljava/lang/Object;", "suspendAndThrow", "", "Ljava/lang/Exception;", "Lkotlin/Exception;", "(Ljava/lang/Exception;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "retrofit"})
@JvmName(name = "KotlinExtensions")
public final class KotlinExtensions {
  @Nullable
  public static final <T> Object await(@NotNull Call $this$await, @NotNull Continuation $completion) {
    int $i$f$suspendCancellableCoroutine = 0;
    Continuation uCont$iv = $completion;
    int $i$a$-suspendCoroutineUninterceptedOrReturn-CancellableContinuationKt$suspendCancellableCoroutine$2$iv = 0;
    CancellableContinuationImpl cancellable$iv = new CancellableContinuationImpl(IntrinsicsKt.intercepted(uCont$iv), 1);
    CancellableContinuation continuation = (CancellableContinuation)cancellable$iv;
    int $i$a$-suspendCancellableCoroutine-KotlinExtensions$await$2 = 0;
    continuation.invokeOnCancellation(new KotlinExtensions$await$$inlined$suspendCancellableCoroutine$lambda$1($this$await));
    $this$await.enqueue(new KotlinExtensions$await$2$2(continuation));
    if (cancellable$iv.getResult() == IntrinsicsKt.getCOROUTINE_SUSPENDED())
      DebugProbesKt.probeCoroutineSuspended($completion); 
    return cancellable$iv.getResult();
  }
  
  @JvmName(name = "awaitNullable")
  @Nullable
  public static final <T> Object awaitNullable(@NotNull Call $this$await, @NotNull Continuation $completion) {
    int $i$f$suspendCancellableCoroutine = 0;
    Continuation uCont$iv = $completion;
    int $i$a$-suspendCoroutineUninterceptedOrReturn-CancellableContinuationKt$suspendCancellableCoroutine$2$iv = 0;
    CancellableContinuationImpl cancellable$iv = new CancellableContinuationImpl(IntrinsicsKt.intercepted(uCont$iv), 1);
    CancellableContinuation continuation = (CancellableContinuation)cancellable$iv;
    int $i$a$-suspendCancellableCoroutine-KotlinExtensions$await$4 = 0;
    continuation.invokeOnCancellation(new KotlinExtensions$await$$inlined$suspendCancellableCoroutine$lambda$2($this$await));
    $this$await.enqueue(new KotlinExtensions$await$4$2(continuation));
    if (cancellable$iv.getResult() == IntrinsicsKt.getCOROUTINE_SUSPENDED())
      DebugProbesKt.probeCoroutineSuspended($completion); 
    return cancellable$iv.getResult();
  }
  
  @Nullable
  public static final <T> Object awaitResponse(@NotNull Call $this$awaitResponse, @NotNull Continuation $completion) {
    int $i$f$suspendCancellableCoroutine = 0;
    Continuation uCont$iv = $completion;
    int $i$a$-suspendCoroutineUninterceptedOrReturn-CancellableContinuationKt$suspendCancellableCoroutine$2$iv = 0;
    CancellableContinuationImpl cancellable$iv = new CancellableContinuationImpl(IntrinsicsKt.intercepted(uCont$iv), 1);
    CancellableContinuation continuation = (CancellableContinuation)cancellable$iv;
    int $i$a$-suspendCancellableCoroutine-KotlinExtensions$awaitResponse$2 = 0;
    continuation.invokeOnCancellation(new KotlinExtensions$awaitResponse$$inlined$suspendCancellableCoroutine$lambda$1($this$awaitResponse));
    $this$awaitResponse.enqueue(new KotlinExtensions$awaitResponse$2$2(continuation));
    if (cancellable$iv.getResult() == IntrinsicsKt.getCOROUTINE_SUSPENDED())
      DebugProbesKt.probeCoroutineSuspended($completion); 
    return cancellable$iv.getResult();
  }
  
  @Nullable
  public static final Object suspendAndThrow(@NotNull Exception $this$suspendAndThrow, @NotNull Continuation $completion) {
    // Byte code:
    //   0: aload_1
    //   1: instanceof retrofit2/KotlinExtensions$suspendAndThrow$1
    //   4: ifeq -> 39
    //   7: aload_1
    //   8: checkcast retrofit2/KotlinExtensions$suspendAndThrow$1
    //   11: astore #5
    //   13: aload #5
    //   15: getfield label : I
    //   18: ldc -2147483648
    //   20: iand
    //   21: ifeq -> 39
    //   24: aload #5
    //   26: dup
    //   27: getfield label : I
    //   30: ldc -2147483648
    //   32: isub
    //   33: putfield label : I
    //   36: goto -> 49
    //   39: new retrofit2/KotlinExtensions$suspendAndThrow$1
    //   42: dup
    //   43: aload_1
    //   44: invokespecial <init> : (Lkotlin/coroutines/Continuation;)V
    //   47: astore #5
    //   49: aload #5
    //   51: getfield result : Ljava/lang/Object;
    //   54: astore #4
    //   56: invokestatic getCOROUTINE_SUSPENDED : ()Ljava/lang/Object;
    //   59: astore #6
    //   61: aload #5
    //   63: getfield label : I
    //   66: tableswitch default -> 185, 0 -> 88, 1 -> 164
    //   88: aload #4
    //   90: invokestatic throwOnFailure : (Ljava/lang/Object;)V
    //   93: aload #5
    //   95: aload_0
    //   96: putfield L$0 : Ljava/lang/Object;
    //   99: aload #5
    //   101: iconst_1
    //   102: putfield label : I
    //   105: aload #5
    //   107: checkcast kotlin/coroutines/Continuation
    //   110: astore_2
    //   111: iconst_0
    //   112: istore_3
    //   113: invokestatic getDefault : ()Lkotlinx/coroutines/CoroutineDispatcher;
    //   116: aload_2
    //   117: invokeinterface getContext : ()Lkotlin/coroutines/CoroutineContext;
    //   122: new retrofit2/KotlinExtensions$suspendAndThrow$$inlined$suspendCoroutineUninterceptedOrReturn$lambda$1
    //   125: dup
    //   126: aload_2
    //   127: aload_0
    //   128: invokespecial <init> : (Lkotlin/coroutines/Continuation;Ljava/lang/Exception;)V
    //   131: checkcast java/lang/Runnable
    //   134: invokevirtual dispatch : (Lkotlin/coroutines/CoroutineContext;Ljava/lang/Runnable;)V
    //   137: invokestatic getCOROUTINE_SUSPENDED : ()Ljava/lang/Object;
    //   140: dup
    //   141: invokestatic getCOROUTINE_SUSPENDED : ()Ljava/lang/Object;
    //   144: if_acmpne -> 155
    //   147: aload #5
    //   149: checkcast kotlin/coroutines/Continuation
    //   152: invokestatic probeCoroutineSuspended : (Lkotlin/coroutines/Continuation;)V
    //   155: dup
    //   156: aload #6
    //   158: if_acmpne -> 180
    //   161: aload #6
    //   163: areturn
    //   164: aload #5
    //   166: getfield L$0 : Ljava/lang/Object;
    //   169: checkcast java/lang/Exception
    //   172: astore_0
    //   173: aload #4
    //   175: invokestatic throwOnFailure : (Ljava/lang/Object;)V
    //   178: aload #4
    //   180: pop
    //   181: getstatic kotlin/Unit.INSTANCE : Lkotlin/Unit;
    //   184: areturn
    //   185: new java/lang/IllegalStateException
    //   188: dup
    //   189: ldc 'call to 'resume' before 'invoke' with coroutine'
    //   191: invokespecial <init> : (Ljava/lang/String;)V
    //   194: athrow
    // Line number table:
    //   Java source line number -> byte code offset
    //   #112	-> 59
    //   #113	-> 93
    //   #114	-> 113
    //   #117	-> 137
    //   #113	-> 140
    //   #112	-> 161
    //   #119	-> 180
    // Local variable table:
    //   start	length	slot	name	descriptor
    //   111	29	2	continuation	Lkotlin/coroutines/Continuation;
    //   113	27	3	$i$a$-suspendCoroutineUninterceptedOrReturn-KotlinExtensions$suspendAndThrow$2	I
    //   0	195	0	$this$suspendAndThrow	Ljava/lang/Exception;
    //   0	195	1	$completion	Lkotlin/coroutines/Continuation;
    //   49	136	5	$continuation	Lkotlin/coroutines/Continuation;
    //   56	129	4	$result	Ljava/lang/Object;
  }
  
  @Metadata(mv = {1, 1, 15}, bv = {1, 0, 3}, k = 3, d1 = {"\000\030\n\000\n\002\020\000\n\002\030\002\n\002\030\002\n\000\n\002\030\002\n\002\020\001\020\000\032\004\030\0010\001*\0060\002j\002`\0032\f\020\004\032\b\022\004\022\0020\0060\005H\u0080@"}, d2 = {"suspendAndThrow", "", "Ljava/lang/Exception;", "Lkotlin/Exception;", "continuation", "Lkotlin/coroutines/Continuation;", ""})
  @DebugMetadata(f = "KotlinExtensions.kt", l = {113}, i = {0}, s = {"L$0"}, n = {"$this$suspendAndThrow"}, m = "suspendAndThrow", c = "retrofit2.KotlinExtensions")
  static final class KotlinExtensions$suspendAndThrow$1 extends ContinuationImpl {
    int label;
    
    Object L$0;
    
    @Nullable
    public final Object invokeSuspend(@NotNull Object $result) {
      this.result = $result;
      this.label |= Integer.MIN_VALUE;
      return KotlinExtensions.suspendAndThrow(null, (Continuation<?>)this);
    }
    
    KotlinExtensions$suspendAndThrow$1(Continuation param1Continuation) {
      super(param1Continuation);
    }
  }
}
