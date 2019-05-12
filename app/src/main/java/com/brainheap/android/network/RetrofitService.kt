package com.brainheap.android.network

import com.brainheap.android.model.User
import com.brainheap.android.model.UserView
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.*


interface RetrofitService {
    @GET("/users")
    fun findUser(@Query("email") email: String): Deferred<Response<User>>

    @POST("/users")
    fun createUser(@Body user: UserView): Deferred<Response<User>>
}