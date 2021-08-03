package cn.udday.forretrofit.PilPiliConverterDemo

data class BaseResponse<T>(
    val `data`: T?,
    val status: Boolean?
)
