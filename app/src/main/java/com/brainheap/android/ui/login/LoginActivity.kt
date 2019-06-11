package com.brainheap.android.ui.login

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.brainheap.android.R
import com.brainheap.android.login.AuthProvider
import com.brainheap.android.login.AuthProviderManager
import com.brainheap.android.login.data.AuthProgressData
import com.brainheap.android.preferences.Constants
import com.brainheap.android.preferences.CredentialsHolder
import kotlinx.android.synthetic.main.fragment_login.*


class LoginActivity : AppCompatActivity() {
    private val providerManager = AuthProviderManager
    private var preferencesLoader = PreferencesLoader(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_login)

        emailEditText.setText(preferencesLoader.get().getString(Constants.NAME_PROP, "Unknown"))

        keycloak_login_button.setOnClickListener {
            updateAuthProvider(AuthProvider.Type.KEYCLOAK_SERVER)
        }
        fb_login_button.setOnClickListener {
            updateAuthProvider(AuthProvider.Type.FACEBOOK)
        }
        google_login_button.setOnClickListener {
            updateAuthProvider(AuthProvider.Type.GOOGLE)
        }
        emailSetButton.setOnClickListener {
            providerManager.loginByEmailOnly(emailEditText.text.toString(), this)
        }

        providerManager.data.observe(this, Observer { data ->
            updateControlsInProgress(data.inProgress)
            updateViewModelByAuthData(data)
        })

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        providerManager.get().onLoginActivityResult(0, 0, intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        providerManager.get().onLoginActivityResult(requestCode, resultCode, intent)
    }

    private fun updateAuthProvider(type: AuthProvider.Type) {
        providerManager.login(type, this)
    }

    private fun updateControlsInProgress(inProgress: Boolean) {
        if (inProgress) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
            loadingSpinner.visibility = View.VISIBLE
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            loadingSpinner.visibility = View.GONE
        }
    }

    private fun updateViewModelByAuthData(data: AuthProgressData) {
        if (!data.inProgress && data.userId?.isNotEmpty() == true) {
            CredentialsHolder.userId.postValue(data.userId)
            CredentialsHolder.email.postValue(data.email)
            preferencesLoader.get().edit().putString(Constants.ID_PROP, data.userId).apply()
            preferencesLoader.get().getString(Constants.NAME_PROP, "Unknown")
            finish()
        }
    }

    internal class PreferencesLoader(val activity: AppCompatActivity) {
        private var preferences: SharedPreferences? = null

        fun get() =
            preferences ?: let {
                preferences = PreferenceManager.getDefaultSharedPreferences(activity)
                preferences
            }!!
    }
}
