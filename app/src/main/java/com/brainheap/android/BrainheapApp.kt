package com.brainheap.android

import android.app.Application
import android.content.Context
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy.ACCEPT_ALL
import java.net.CookieStore

class BrainheapApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CookieHandler.setDefault(CookieManager(null, ACCEPT_ALL))
        instance = this
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