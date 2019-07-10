package com.brainheap.android.network

import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

abstract class RefrofitClientFactory<T>(private val clazz: Class<T>, val baseUrl: String) {
    private var client: T? = null

    fun get(): T = client ?: create()

    private fun create(): T {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        client = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(HttpClientFactory.instance())
            .build().create(clazz)
        return client!!
    }
}