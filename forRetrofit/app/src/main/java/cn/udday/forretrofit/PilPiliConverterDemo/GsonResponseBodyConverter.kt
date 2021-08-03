package cn.udday.forretrofit.PilPiliConverterDemo

import android.util.Log
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import okhttp3.ResponseBody
import retrofit2.Converter
import java.io.IOException

class GsonResponseBodyConverter<T>(private val gson: Gson,private val adapter: TypeAdapter<T>):Converter<ResponseBody,T> {
    override fun convert(value: ResponseBody): T? {
        val jsonReader: JsonReader = gson.newJsonReader(value.charStream())
            val baseAdapter = gson.getAdapter(TypeToken.get(BaseResponse::class.java))
            val result = baseAdapter.read(jsonReader)
            if (result.status == true) {
                return adapter.read(jsonReader)
            }else{
                Log.e("Retrofit",result.data.toString())
                throw IOException("result error ${result.data}")
            }
        return null
    }
}