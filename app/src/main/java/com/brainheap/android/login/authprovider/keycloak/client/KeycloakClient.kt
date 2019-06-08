package com.brainheap.android.login.authprovider.keycloak.client

import com.brainheap.android.config.KeycloakProperties
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import java.util.*

object KeycloakClientFactory {
    private var client: KeycloakClient? = null

    fun get(): KeycloakClient = client ?: create()

    private fun create(): KeycloakClient {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        client = Retrofit.Builder()
            .baseUrl("${KeycloakProperties.baseUrl}/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build().create(KeycloakClient::class.java)

        return client!!
    }
}

interface KeycloakClient {
    @POST("token")
    @FormUrlEncoded
    fun grantNewAccessTokenAsync(
        @Field("code") code: String,
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("redirect_uri") uri: String,
        @Field("grant_type") grantType: String = "authorization_code"
    ): Deferred<Response<KeycloakToken>>

    @POST("token")
    @FormUrlEncoded
    fun refreshAccessTokenAsync(
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientId: String,
        @Field("grant_type") grantType: String = "refresh_token"
    ): Deferred<Response<KeycloakToken>>

    @POST("userinfo")
    @FormUrlEncoded
    fun getUserInfoAsync(
        @Field("access_token") accessToken: String
    ): Deferred<Response<KeycloakUserInfo>>

    @POST("logout")
    @FormUrlEncoded
    fun logoutAsync(
        @Field("client_id") clientId: String,
        @Field("refresh_token") refreshToken: String
    ): Deferred<Response<String>>
}

data class KeycloakToken(
    @SerializedName("access_token") var accessToken: String? = null,
    @SerializedName("expires_in") var expiresIn: Int? = null,
    @SerializedName("refresh_expires_in") var refreshExpiresIn: Int? = null,
    @SerializedName("refresh_token") var refreshToken: String? = null,
    @SerializedName("token_type") var tokenType: String? = null,
    @SerializedName("id_token") var idToken: String? = null,
    @SerializedName("not-before-policy") var notBeforePolicy: Int? = null,
    @SerializedName("session_state") var sessionState: String? = null,
    @SerializedName("token_expiration_date") var tokenExpirationDate: Calendar? = null,
    @SerializedName("refresh_expiration_date") var refreshTokenExpirationDate: Calendar? = null
)

data class KeycloakUserInfo(
    @SerializedName("email") var email: String? = null
)