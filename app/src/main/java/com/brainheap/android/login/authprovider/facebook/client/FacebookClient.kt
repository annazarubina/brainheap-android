package com.brainheap.android.login.authprovider.facebook.client

import com.brainheap.android.config.FacebookProperties
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

object FacebookClientFactory {
    private var client: FacebookClient? = null

    fun get(): FacebookClient = client ?: create()

    private fun create(): FacebookClient {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        client = Retrofit.Builder()
            .baseUrl(FacebookProperties.baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build().create(FacebookClient::class.java)

        return client!!
    }
}

interface FacebookClient {
    @GET("{userId}")
    fun getUserInfoAsync(
        @Path(value = "userId") userId: String,
        @Query("access_token") token: String,
        @Query("fields") grantType: String = "id,name,email"
    ): Deferred<Response<FacebookUserInfo>>
}

data class FacebookUserInfo(
    @SerializedName("id") var id: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("email") var email: String? = null
)