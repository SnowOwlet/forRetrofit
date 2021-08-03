package cn.udday.forretrofit.api

import cn.udday.forretrofit.bean.UserInfoBean
import cn.udday.forretrofit.bean.VideoBean
import cn.udday.forretrofit.callAdapterDemo.MyCall
import io.reactivex.Observable
import retrofit2.http.*

interface AllApi {

    @GET("user/info/{uid}")
    fun getUserInfoByUid(@Path("uid") uid:Int): Observable<UserInfoBean>


    @FormUrlEncoded
    @POST("user/login/pw")
    fun login(@Field("loginName") loginName: String, @Field("password") password:String):Observable<String>

//    @GET("video/video")
//    fun getVideoById(@Query("video_id") videoId:Int): MyCall<VideoBean>
//    fun name(){
//        println("报错")
//    }
}