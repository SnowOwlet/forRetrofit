package cn.udday.forretrofit.callAdapterDemo

import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class MyCallAdapterFactrory: CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        val rawType = getRawType(returnType)
        //检测是否是MyCall类型，并且得有泛型
        if (rawType === MyCall::class.java && returnType is ParameterizedType){
            //ParameterizedType-->>泛型
            val parameterUpperBound = getParameterUpperBound(0, returnType)
            //创建一个CallAdapter
            return  MyCallAdapter(parameterUpperBound)
        }
        return null
    }
}