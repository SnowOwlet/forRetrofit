package cn.udday.forretrofit.converterDemo

import okhttp3.ResponseBody
import retrofit2.Converter

class MyConverter: Converter<ResponseBody,String> {
    override fun convert(value: ResponseBody): String? {
        return value.string()+"\n 私货"
    }
}