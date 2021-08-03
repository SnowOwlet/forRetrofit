package cn.udday.forretrofit.myProxy

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import java.util.*

fun main(){
    System.getProperties().put("jdk.proxy.ProxyGenerator.saveGeneratedFiles", "true")
    val realSubject = RealSubject()
    val classLoader = realSubject.javaClass.classLoader
    val interfaces = realSubject.javaClass.interfaces
    val logHandler:InvocationHandler = LogHandler(realSubject)
    val newProxyInstance = Proxy.newProxyInstance(classLoader, interfaces, logHandler) as Subject
    newProxyInstance.request()
}