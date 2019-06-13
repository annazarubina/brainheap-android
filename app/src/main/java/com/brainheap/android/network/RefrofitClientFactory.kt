package com.brainheap.android.network

import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.ParameterizedType

abstract class RefrofitClientFactory<T>(val baseUrl: String) {
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
            .build().create(
                (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>)
        return client!!
    }
}