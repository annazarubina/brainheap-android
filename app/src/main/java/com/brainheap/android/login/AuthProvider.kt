package com.brainheap.android.login

import android.app.Activity
import android.content.Intent
import com.brainheap.android.login.data.AuthProgressData

abstract class AuthProvider(val data: AuthProgressData) {
    enum class Type(val string: String) {
        GOOGLE("GOOGLE"),
        FACEBOOK("FACEBOOK"),
        KEYCLOAK_SERVER("KEYCLOAK_SERVER")
    }

    protected var activity: Activity? = null

    fun login(activity: Activity) {
        this.activity = activity
        doLogin()
    }

    fun onLoginActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        doOnLoginActivityResult(requestCode, resultCode, intent)
    }

    protected abstract fun doLogin()
    protected abstract fun doOnLoginActivityResult(requestCode: Int, resultCode: Int, intent: Intent?)

    abstract fun logout()

    abstract fun getRequestCode(): Int
}