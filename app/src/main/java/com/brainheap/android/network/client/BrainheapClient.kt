package com.brainheap.android.network.client

import com.brainheap.android.config.BrainheapProperties
import com.brainheap.android.model.Item
import com.brainheap.android.model.ItemView
import com.brainheap.android.model.User
import com.brainheap.android.model.UserView
import com.brainheap.android.network.RefrofitClientFactory
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

object BrainheapClientFactory :
    RefrofitClientFactory<BrainheapClient>(BrainheapClient::class.java, BrainheapProperties.baseUrl)

interface BrainheapClient {
    @GET("/users")
    fun findUserAsync(@Query("email") email: String): Deferred<Response<List<User>>>

    @POST("/users")
    fun createUserAsync(@Body user: UserView): Deferred<Response<User>>

    @POST("/items/new")
    fun createItem(@Header("Authorization") userId: String, @Body item: ItemView): Call<Item>

    @PATCH("/items/{itemId}")
    fun updateItem(@Header("Authorization") userId: String, @Path("itemId") itemId:String, @Body item: ItemView): Call<Item>

    @GET("/items")
    fun findItemsAsync(@Header("Authorization") userId: String, @Query("query") query: String?): Deferred<Response<List<Item>>>

    @DELETE("/items/{itemId}")
    fun deleteItem(@Header("Authorization") userId: String, @Path("itemId") itemId:String): Call<Item>

    @GET("/translate")
    fun translateAsync(@Header("Authorization") userId: String, @Query("srcString") srcString: String ): Deferred<Response<String>>
}