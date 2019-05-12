package com.brainheap.android.network

import com.brainheap.android.model.Item
import com.brainheap.android.model.ItemView
import com.brainheap.android.model.User
import com.brainheap.android.model.UserView
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.*


interface RetrofitService {
    @GET("/users")
    fun findUserAsync(@Query("email") email: String): Deferred<Response<User>>

    @POST("/users")
    fun createUserAsync(@Body user: UserView): Deferred<Response<User>>

    @POST("/items/new")
    fun createItemAsync(@Header("Authorization") userId: String, @Body item: ItemView): Deferred<Response<Item>>
}