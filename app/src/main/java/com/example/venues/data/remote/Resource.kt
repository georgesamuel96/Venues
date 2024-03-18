package com.example.venues.data.remote

data class Resource<out T>(
    val status: Status,
    val data: T?,
    val message:String?,
    val showAlert: Boolean = true
){
    companion object{

        fun <T> success(data:T?): Resource<T>{
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> error(msg:String, showAlert: Boolean = true): Resource<T>{
            return Resource(Status.ERROR, null, msg, showAlert)
        }
    }
}