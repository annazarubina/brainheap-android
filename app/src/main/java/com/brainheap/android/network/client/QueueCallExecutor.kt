package com.brainheap.android.network.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import retrofit2.Call
import java.util.*

object QueueCallExecutor {
    private val queue: Queue<Data<*>> = LinkedList<Data<*>>()
    private var job: Job? = null

    @Synchronized
    fun <T> add(data: Data<T>) {
        queue.add(data)
    }

    fun start() {
        job?.let {
            return
        } ?: let {
            val scope = CoroutineScope(newSingleThreadContext(this.javaClass.name))
            job = scope.launch {
                while (true) {
                    queue.peek()
                        ?.let { execute(it) }
                        ?.takeIf { it }
                        ?.let { queue.poll() }
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private fun execute(data: Data<*>): Boolean {
        return try {
            val response = data.call.clone().execute()
            when (response.isSuccessful) {
                true -> data.callback.onSuccess()
                else -> data.callback.onError("Error: returned code = ${response.code()}")
            }
            response.isSuccessful
        } catch (e: Throwable) {
            data.callback.onError("Error: exception \"${e.message}\"")
            false
        }
    }

    interface Callback {
        fun onSuccess()
        fun onError(message: String)
    }

    class Data<T>(val call: Call<T>, val callback: Callback)
}