package myProxy

class SubjectProxy:Subject {

    fun request(){
        beforeRequest()
        RealSubject().request()
        afterRequest()
    }

    fun beforeRequest(){
        println("beforeRequest")
    }

    fun afterRequest(){
        println("afterRequest")
    }
}