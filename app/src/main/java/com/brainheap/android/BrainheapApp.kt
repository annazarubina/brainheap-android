package com.brainheap.android

import android.app.Application
import android.content.Context

class BrainheapApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        @JvmStatic
        private var instance: BrainheapApp? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }
}