package com.brainheap.android

import android.app.Application
import android.content.Context
import com.brainheap.android.network.client.QueueCallExecutor

class BrainheapApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        QueueCallExecutor.start()
    }

    companion object {
        @JvmStatic
        private var instance: BrainheapApp? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }
}