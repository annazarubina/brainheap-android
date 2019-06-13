package com.brainheap.android

import android.app.Application
import android.content.Context
import com.brainheap.android.network.client.QueueCallExecutor
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger

class BrainheapApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        QueueCallExecutor.start()
    }

    companion object {
        @JvmStatic
        private var instance: BrainheapApp? = null

        fun application() : BrainheapApp {
            return instance!!
        }

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }
}