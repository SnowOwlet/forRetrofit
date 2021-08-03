package cn.udday.forretrofit

import android.util.Log
import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody

class MyInterceptor:Interceptor {
    private val TAG = "MyInterceptor"
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startTime = System.currentTimeMillis()
        val response = chain.proceed(request)
        val endTime = System.currentTimeMillis()
        val userTime = endTime - startTime
        val content = response.body?.string()
        val requestMeathod = request.method
        Log.d(TAG,"\n -------------START-------------")
        Log.d(TAG,"花费了$userTime")
        Log.d(TAG,"请求方法 $requestMeathod")
        Log.d(TAG,"返回信息 \n $content")
        Log.d(TAG,"-------------START-------------")
        return response
    }
}