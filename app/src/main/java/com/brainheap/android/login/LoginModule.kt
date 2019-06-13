package com.brainheap.android.login

import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import com.brainheap.android.preferences.AppPreferences
import com.brainheap.android.preferences.Constants
import com.brainheap.android.preferences.CredentialsHolder
import com.brainheap.android.ui.login.LoginActivity

class LoginModule {
    private val providerManager = AuthProviderManager

    fun isLoggedIn() = CredentialsHolder.userId.value?.isNotEmpty() ?: false

    fun logIn(activity: AppCompatActivity) = activity.startActivity(Intent(activity, LoginActivity::class.java))

    fun logOut() {
        AppPreferences.get().edit().putString(Constants.ID_PROP, "").apply()
        AuthProviderManager.logout()
        CredentialsHolder.clean()
    }
}