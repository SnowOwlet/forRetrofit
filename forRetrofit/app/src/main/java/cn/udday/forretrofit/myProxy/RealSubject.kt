package cn.udday.forretrofit.myProxy

class RealSubject:Subject {
    override fun request(){
        println("真实的请求")
    }
}