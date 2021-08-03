package retrofit2;

public interface Callback<T> {
  void onResponse(Call<T> paramCall, Response<T> paramResponse);
  
  void onFailure(Call<T> paramCall, Throwable paramThrowable);
}
