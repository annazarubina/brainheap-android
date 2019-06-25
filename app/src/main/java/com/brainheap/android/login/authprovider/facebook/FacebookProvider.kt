package com.brainheap.android.login.authprovider.facebook

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.brainheap.android.BrainheapApp
import com.brainheap.android.login.AuthProvider
import com.brainheap.android.login.data.AuthProgressData
import com.brainheap.android.login.data.OAuthUserData
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.GraphRequest
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class FacebookProvider(data: MutableLiveData<AuthProgressData>) : AuthProvider(data) {
    private lateinit var fbLoginManager: LoginManager
    private lateinit var callbackManager: CallbackManager

    private fun init() {
        FacebookSdk.sdkInitialize(BrainheapApp.applicationContext())
        AppEventsLogger.activateApp(BrainheapApp.application())

        fbLoginManager = LoginManager.getInstance()
        callbackManager = CallbackManager.Factory.create()

        fbLoginManager.registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Toast.makeText(
                        BrainheapApp.applicationContext(),
                        "Facebook token: " + loginResult.accessToken.token,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    getAuthUserEmail(loginResult.accessToken)
                }

                override fun onCancel() {
                    Toast.makeText(BrainheapApp.applicationContext(), "Facebook: onCancel", Toast.LENGTH_SHORT).show()
                    onLoginFailed()
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(BrainheapApp.applicationContext(), "Facebook: onError", Toast.LENGTH_SHORT).show()
                    onLoginFailed()
                }
            }
        )
    }

    override fun doLogin() {
        init()
        fbLoginManager.logInWithReadPermissions(activity, Arrays.asList("public_profile", "email"))
    }

    override fun doOnLoginActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, intent)
    }

    override fun getRequestCode(): Int = 9002

    private fun getAuthUserEmail(accessToken: AccessToken) {
        CoroutineScope(Dispatchers.IO).launch {
            var toastMessage: String? = null
            val request = GraphRequest.newMeRequest(accessToken) { `object`, response ->
                require(response.connection.responseCode == 200) { "Get user info failed: ${response.connection.responseCode}" }
                try {
                    val email = `object`.get("email") as String?
                    require(email?.isNotEmpty() ?: false) { "Email is empty" }
                    onLoginSuccess(
                        OAuthUserData(
                            email,
                            accessToken.token
                        )
                    )
                } catch (e: Exception) {
                    toastMessage = "Exception ${e.message}"
                    onLoginFailed()
                }
            }

            val parameters = Bundle()
            parameters.putString("fields", "email")
            request.parameters = parameters
            request.executeAndWait()

            toastMessage?.let {
                withContext(Dispatchers.Main) {
                    Toast.makeText(BrainheapApp.applicationContext(), "Facebook auth error: $it", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}