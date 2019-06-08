package com.brainheap.android.login.authprovider.facebook

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.brainheap.android.login.AuthProvider
import com.brainheap.android.login.authprovider.facebook.client.FacebookUserInfo
import com.brainheap.android.login.data.AuthProgressData
import com.brainheap.android.ui.login.OAuthUserData
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk.getApplicationContext
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.*

class FacebookProvider(data: AuthProgressData) : AuthProvider(data) {
    private val fbLoginManager: LoginManager = LoginManager.getInstance()
    private val callbackManager: CallbackManager = CallbackManager.Factory.create()
    private val mapper = ObjectMapper()

    private fun init() {
        fbLoginManager.registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Toast.makeText(getApplicationContext(), "Facebook token: " + loginResult.accessToken.token, Toast.LENGTH_SHORT)
                        .show()
                    getAuthUserEmail(loginResult.accessToken.userId, loginResult.accessToken.token)
                }

                override fun onCancel() {
                    Toast.makeText(getApplicationContext(), "Facebook: onCancel", Toast.LENGTH_SHORT).show()
                    data.onFailed()
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(getApplicationContext(), "Facebook: onError", Toast.LENGTH_SHORT).show()
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
            val graphUrl =
                URL("https://graph.facebook.com/${userId}?fields=id,name,email&access_token=${token}")
            try {
                val connection = graphUrl.openConnection()
                val inputStream = BufferedReader(InputStreamReader(connection.getInputStream()))
                val buffer = StringBuffer()
                var line = inputStream.readLine()
                while (line != null) {
                    buffer.append(line)
                    line = inputStream.readLine()
                }
                inputStream.close()
                val userData = mapper.readValue(buffer.toString(), FacebookUserInfo::class.java)
                data.onSuccess(
                    OAuthUserData(
                        userData.email,
                        token
                    )
                )
            } catch (e: Throwable) {
                toastMessage = "Exception ${e.message}"
                data.onFailed()
            }
            toastMessage?.let {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplicationContext(), "Facebook auth error: $it", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}