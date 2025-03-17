package com.majdoor.ovr.shramik.app.utils

sealed class Response<T>(
    val data :T?=null,
    val message:String?=null
){
    class Init<T>: Response<T>(null, null)
    class Loading<T>: Response<T>()
    class Success<T>(data: T?): Response<T>(data = data)
    class Error<T>(errorMessage: String?): Response<T>(message = errorMessage)
}