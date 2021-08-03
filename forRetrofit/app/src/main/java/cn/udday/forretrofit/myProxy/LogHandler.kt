package cn.udday.forretrofit.myProxy

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.util.*

class LogHandler(private val delegate:Any):InvocationHandler {

    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
        before()
        val invoke = method?.invoke(delegate,*args.orEmpty()) as Any?
        after()
        return invoke
    }
    fun before(){
        println("Before")
    }
    fun after(){
        println("after")
    }
}