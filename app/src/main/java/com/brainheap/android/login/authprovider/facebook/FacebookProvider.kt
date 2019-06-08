package com.brainheap.android.login.authprovider.facebook

import android.content.Intent
import android.widget.Toast
import com.brainheap.android.BrainheapApp
import com.brainheap.android.login.AuthProvider
import com.brainheap.android.login.authprovider.facebook.client.FacebookClientFactory
import com.brainheap.android.login.data.AuthProgressData
import com.brainheap.android.ui.login.OAuthUserData
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class FacebookProvider(data: AuthProgressData) : AuthProvider(data) {
    private val fbLoginManager: LoginManager = LoginManager.getInstance()
    private val callbackManager: CallbackManager = CallbackManager.Factory.create()

    private fun init() {
        fbLoginManager.registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Toast.makeText(
                        BrainheapApp.applicationContext(),
                        "Facebook token: " + loginResult.accessToken.token,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    getAuthUserEmail(loginResult.accessToken.userId, loginResult.accessToken.token)
                }

                override fun onCancel() {
                    Toast.makeText(BrainheapApp.applicationContext(), "Facebook: onCancel", Toast.LENGTH_SHORT).show()
                    data.onFailed()
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(BrainheapApp.applicationContext(), "Facebook: onError", Toast.LENGTH_SHORT).show()
                    data.onFailed()
                }
            }
        )
    }

    override fun doLogin() {
        init()
        data.start()
        fbLoginManager.logInWithReadPermissions(activity, Arrays.asList("public_profile", "email"))
    }

    override fun doOnLoginActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, intent)
    }

    override fun logout() {}

    override fun getRequestCode(): Int = 9002

    private fun getAuthUserEmail(userId: String, token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            var toastMessage: String? = null
            try {
                val request = FacebookClientFactory.get().getUserInfoAsync(userId, token)
                val response = request.await()
                require(response.isSuccessful) { "Get user info failed: ${response.code()}" }
                val email = response.body()?.email
                require(email?.isNotEmpty() ?: false) { "Email is empty" }
                data.onSuccess(
                    OAuthUserData(
                        email,
                        token
                    )
                )
            } catch (e: Throwable) {
                toastMessage = "Exception ${e.message}"
                data.onFailed()
            }
            toastMessage?.let {
                withContext(Dispatchers.Main) {
                    Toast.makeText(BrainheapApp.applicationContext(), "Facebook auth error: $it", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}