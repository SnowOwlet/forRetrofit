package cn.udday.forretrofit.callAdapterDemo

import retrofit2.Call


class MyCall<R> constructor(private val call:Call<Any>) {

    fun get(): Any? {//直接执行传入的call,这里可以夹带私活
        return call.execute().body()
    }
}