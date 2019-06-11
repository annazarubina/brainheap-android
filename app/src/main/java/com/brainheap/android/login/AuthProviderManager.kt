package com.brainheap.android.login

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import com.brainheap.android.login.authprovider.facebook.FacebookProvider
import com.brainheap.android.login.authprovider.google.GoogleProvider
import com.brainheap.android.login.authprovider.keycloak.KeycloakProvider
import com.brainheap.android.login.data.AuthProgressData

object AuthProviderManager {
    private var active = AuthProvider.Type.KEYCLOAK_SERVER
    private var providers: MutableMap<AuthProvider.Type, AuthProvider> = HashMap()
    val data = MutableLiveData<AuthProgressData>()

    fun get(): AuthProvider =
        providers[active] ?: let {
            createProvider(active).let {
                providers[active] = it
                it
            }
        }

    fun login(type: AuthProvider.Type, activity: Activity) {
        switch(type)
        get().login(activity)
    }

    fun logout() {
        get().logout()
    }

    fun loginByEmailOnly(email: String?, activity: Activity) {
        get().loginByEmailOnly(email, activity)
    }

    private fun switch(type: AuthProvider.Type): AuthProvider {
        this.active = type
        return get()
    }

    private fun createProvider(type: AuthProvider.Type): AuthProvider =
        when (type) {
            AuthProvider.Type.GOOGLE -> GoogleProvider(data)
            AuthProvider.Type.FACEBOOK -> FacebookProvider(data)
            AuthProvider.Type.KEYCLOAK_SERVER -> KeycloakProvider(data)
        }
}