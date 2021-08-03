package cn.udday.forretrofit.callAdapterDemo

import cn.udday.forretrofit.R
import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

class MyCallAdapter(private val responseType: Type ):CallAdapter<Any, MyCall<R>> {
    //这里因为没有什么业务处理，所以内容很少，最后的执行还是在MyCall里面执行的
    override fun responseType(): Type {
        return responseType
    }

    override fun adapt(call: Call<Any>): MyCall<R> {
        //这里可以进行操作
       return MyCall(call)
    }
}