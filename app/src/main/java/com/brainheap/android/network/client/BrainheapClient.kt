package com.brainheap.android.network.client

import com.brainheap.android.model.Item
import com.brainheap.android.model.ItemView
import com.brainheap.android.model.User
import com.brainheap.android.model.UserView
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.*


interface BrainheapClient {
    @GET("/users")
    fun findUserAsync(@Query("email") email: String): Deferred<Response<List<User>>>

    @POST("/users")
    fun createUserAsync(@Body user: UserView): Deferred<Response<User>>

    @POST("/items/new")
    fun createItemAsync(@Header("Authorization") userId: String, @Body item: ItemView): Deferred<Response<Item>>

    @PATCH("/items/{itemId}")
    fun updateItemAsync(@Header("Authorization") userId: String, @Path("itemId") itemId:String, @Body item: ItemView): Deferred<Response<Item>>

    @GET("/items")
    fun findItemsAsync(@Header("Authorization") userId: String, @Query("query") query: String?): Deferred<Response<List<Item>>>

    @DELETE("/items/{itemId}")
    fun deleteItemAsync(@Header("Authorization") userId: String, @Path("itemId") itemId:String): Deferred<Response<Item>>

    @GET("/translate")
    fun translateAsync(@Header("Authorization") userId: String, @Query("srcString") srcString: String ): Deferred<Response<String>>
}