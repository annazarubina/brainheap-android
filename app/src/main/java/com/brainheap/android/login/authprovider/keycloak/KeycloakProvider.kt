package com.brainheap.android.login.authprovider.keycloak

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.brainheap.android.BrainheapApp
import com.brainheap.android.config.KeycloakProperties
import com.brainheap.android.login.AuthProvider
import com.brainheap.android.login.authprovider.keycloak.client.KeycloakClientFactory
import com.brainheap.android.login.data.AuthProgressData
import com.brainheap.android.ui.login.OAuthUserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KeycloakProvider(data: AuthProgressData) : AuthProvider(data) {
    private val client = KeycloakClientFactory.get()

    private val authCodeUrl = Uri.parse(KeycloakProperties.authCodeUrl)
        .buildUpon()
        .appendQueryParameter("client_id", KeycloakProperties.clientId)
        .appendQueryParameter("client_secret", KeycloakProperties.clientSecret)
        .appendQueryParameter("redirect_uri", KeycloakProperties.redirectUri)
        .appendQueryParameter("response_type", "code")
        .build()

    override fun doLogin() {
        activity!!.startActivityForResult(Intent(Intent.ACTION_VIEW, authCodeUrl), getRequestCode())
    }

    override fun doOnLoginActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        intent?.data
            ?.takeIf { it.toString().startsWith(KeycloakProperties.redirectUri) }
            ?.let { it.getQueryParameter("code") }
            ?.let { exchangeCodeForToken(it) }
    }

    override fun logout() {
    }

    @SuppressLint("CheckResult")
    private fun exchangeCodeForToken(code: String) {
        CoroutineScope(Dispatchers.IO).launch {
            var toastMessage: String? = null
            try {
                val tokenRequest = client.grantNewAccessTokenAsync(
                    code,
                    KeycloakProperties.clientId,
                    KeycloakProperties.clientSecret,
                    KeycloakProperties.redirectUri
                )
                val tokenResponse = tokenRequest.await()
                require(tokenResponse.isSuccessful) { "Grant new access token failed: ${tokenResponse.code()}" }
                val token = tokenResponse.body()?.accessToken
                require(token?.isNotEmpty() ?: false) { "Grant new access token is empty" }
                val userInfoRequest = client.getUserInfoAsync(token!!)
                val userInfoResponse = userInfoRequest.await()
                require(userInfoResponse.isSuccessful) { "Get user info failed: ${tokenResponse.code()}" }
                val email = userInfoResponse.body()?.email
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
                    Toast.makeText(BrainheapApp.applicationContext(), "Error: $it", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun getRequestCode(): Int = 9003
}