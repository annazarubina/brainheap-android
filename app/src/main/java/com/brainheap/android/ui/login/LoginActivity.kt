package com.brainheap.android.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.brainheap.android.R
import com.brainheap.android.login.AuthProvider
import com.brainheap.android.login.data.AuthProgressData
import com.brainheap.android.preferences.AppPreferences
import com.brainheap.android.preferences.Constants
import com.brainheap.android.preferences.CredentialsHolder
import com.brainheap.android.ui.MainActivity
import kotlinx.android.synthetic.main.fragment_login.*


class LoginActivity : AppCompatActivity() {
    private val provider = AuthProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_login)
        login_button.setOnClickListener {
            provider.login(this)
        }
        provider.data.observe(this, Observer { data ->
            updateControlsInProgress(data.inProgress)
            updateViewModelByAuthData(data)
        })

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        provider.onLoginActivityResult(0, 0, intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        provider.onLoginActivityResult(requestCode, resultCode, intent)
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
            CredentialsHolder.jSessionId.postValue(data.jSessionId)
            AppPreferences.get().edit().putString(Constants.ID_PROP, data.userId).apply()
            AppPreferences.get().getString(Constants.NAME_PROP, "Unknown")
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    override fun onBackPressed() {}
}
