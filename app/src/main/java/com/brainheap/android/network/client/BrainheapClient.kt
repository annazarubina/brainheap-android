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
    @GET("/currentuser")
    fun getCurrentUser(): Call<String>

    @GET("/users")
    fun findUser(@Query("email") email: String): Call<List<User>>

    @POST("/users")
    fun createUser(@Body user: UserView): Call<User>

    @POST("/items/new")
    fun createItem(@Body item: ItemView): Call<Item>

    @PATCH("/items/{itemId}")
    fun updateItem(@Path("itemId") itemId:String, @Body item: ItemView): Call<Item>

    @GET("/items")
    fun findItemsAsync(@Query("query") query: String?): Deferred<Response<List<Item>>>

    @DELETE("/items/{itemId}")
    fun deleteItem(@Path("itemId") itemId:String): Call<Item>

    @GET("/translate")
    fun translateAsync(@Query("srcString") srcString: String ): Deferred<Response<String>>
}