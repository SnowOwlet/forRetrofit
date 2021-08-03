package cn.udday.forretrofit.api;

import cn.udday.forretrofit.bean.UserInfoBean;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface AllApiForJava {
    @GET("user/info/{uid}")
    Observable<UserInfoBean> getUserInfoByUid(@Path("uid")int uid);

    default void test(){
        System.out.println(" 显然这里是可以直接执行的");
    }
}
