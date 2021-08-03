package cn.udday.forretrofit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import cn.udday.forretrofit.PilPiliConverterDemo.BaseResponse
import cn.udday.forretrofit.api.AllApi
import cn.udday.forretrofit.api.AllApiForJava
import cn.udday.forretrofit.bean.UserInfoBean
import cn.udday.forretrofit.bean.VideoBean
import cn.udday.forretrofit.di.ApiModule
import cn.udday.forretrofit.myProxy.LogHandler
import cn.udday.forretrofit.myProxy.RealSubject
import cn.udday.forretrofit.myProxy.Subject
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val testView:TextView = findViewById(R.id.test_text)
        val api = ApiModule.allApi
        GlobalScope.launch {
            /**
             *  CallAdapterDemo
             */
//            println("$TAG ${api.getVideoById(2).get().toString()}")

//            api.getUserInfoByUid(1).observeOn(AndroidSchedulers.mainThread())
//                .subscribe( object:Observer<UserInfoBean>{
//                    override fun onSubscribe(d: Disposable) {
//
//                    }
//
//                    override fun onNext(t: UserInfoBean) {
//                        println("$TAG   ${t.data.Avatar}")
//                    }
//
//                    override fun onError(e: Throwable) {
//                        println(TAG+e.toString())
//                    }
//
//                    override fun onComplete() {
//                        println("$TAG 已经结束")
//                    }
//
//                })

//            api.login("13983617205","abc123456").observeOn(AndroidSchedulers.mainThread())
//                .subscribe(object :Observer<String>{
//                    override fun onSubscribe(d: Disposable) {
//
//                    }
//
//                    override fun onNext(t: String) {
//                        println(TAG + t)
//                    }
//
//                    override fun onError(e: Throwable) {
//                    }
//
//                    override fun onComplete() {
//
//                    }
//
//                })


//            api.getVideoById(1).observeOn(AndroidSchedulers.mainThread()).subscribe(object :Observer<BaseResponse<VideoBean>>{
//                override fun onNext(t: BaseResponse<VideoBean>) {
//                    testView.text = t.data.Cover
//                    println(t.data.Cover)
//                }
//
//                override fun onSubscribe(d: Disposable) {
//
//                }
//
//                override fun onError(e: Throwable) {
//                }
//
//                override fun onComplete() {
//                }
//
//            })


            /**
             * DEFAULT DEMO
             */
//            api.test()
        }


    }
}