  package cn.udday.forretrofit.di

import cn.udday.forretrofit.MyInterceptor
import cn.udday.forretrofit.PilPiliConverterDemo.PilPiliConverterFactory
import cn.udday.forretrofit.api.AllApi
import cn.udday.forretrofit.api.AllApiForJava
import cn.udday.forretrofit.callAdapterDemo.MyCallAdapterFactrory
import cn.udday.forretrofit.converterDemo.MyConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object ApiModule {
    private val BASE_URL = "https://anonym.ink/api/"
    private val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    private val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

    private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .validateEagerly(true)
//        .addConverterFactory(MyConverterFactory())
        .addConverterFactory(GsonConverterFactory.create())
//        .addCallAdapterFactory(MyCallAdapterFactrory())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

    val allApi: AllApi = retrofit.create(AllApi::class.java)
    val allApiForJava: AllApiForJava = retrofit.create(AllApiForJava::class.java)
}