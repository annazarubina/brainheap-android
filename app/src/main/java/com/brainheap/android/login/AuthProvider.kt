package com.brainheap.android.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.brainheap.android.BrainheapApp
import com.brainheap.android.config.BrainheapProperties
import com.brainheap.android.login.data.AuthProgressData
import com.brainheap.android.login.data.OAuthUserData
import com.brainheap.android.model.UserView
import com.brainheap.android.network.HttpClientFactory
import com.brainheap.android.network.client.BrainheapClientFactory
import com.brainheap.android.ui.login.WebLoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

@SuppressLint("StaticFieldLeak")
object AuthProvider {
    val data = MutableLiveData<AuthProgressData>()

    private val client = BrainheapClientFactory.get()

    private var activity: Activity? = null

    fun login(activity: Activity) {
        clean()
        this.activity = activity
        start()
        doLogin()
    }

    fun logout() {
        clean()
    }

    fun onLoginActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        doOnLoginActivityResult(requestCode, resultCode, intent)
    }

    private fun onLoginSuccess(value: OAuthUserData) {
        loadUserId(value)
    }

    private fun onLoginFailed() {
        onFailed()
    }

    private fun loadUserId(value: OAuthUserData?) {
        value?.email?.let { email ->
            CoroutineScope(Dispatchers.IO).launch {
                var userId: String? = null
                val retrofitService = BrainheapClientFactory.get()
                var toastMessage: String? = null
                try {
                    val findUserResponse = retrofitService.findUser(email).execute()
                    if (findUserResponse.isSuccessful) {
                        userId = findUserResponse.body()?.firstOrNull()?.id
                    } else if (findUserResponse.code() == 404) {
                        val createUserResponse = retrofitService.createUser(UserView(email, email)).execute()
                        if (createUserResponse.isSuccessful) {
                            userId = createUserResponse.body()?.id
                        } else {
                            toastMessage = "CreateUser failed:${createUserResponse.code()}"
                        }
                    } else {
                        toastMessage = "FindUser failed:${findUserResponse.code()}"

                    }
                } catch (e: HttpException) {
                    toastMessage = "Exception ${e.message}"
                } catch (e: Throwable) {
                    toastMessage = "Exception ${e.message}"
                }
                toastMessage?.let {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activity, "Error: $it", Toast.LENGTH_SHORT).show()
                    }
                }
                userId
                    ?.let { onSuccess(userId, value) }
                    ?: let { onFailed() }
            }
        } ?: let { onFailed() }
    }

    private fun onSuccess(userId: String, oAuthData: OAuthUserData) {
        data.postValue(AuthProgressData(userId, oAuthData.email, oAuthData.jSessionId, false))
    }

    private fun onFailed() {
        clean()
    }

    private fun clean() {
        data.postValue(AuthProgressData(null, null, null, false))
    }

    private fun start() {
        data.postValue(AuthProgressData(null, null, null, true))
    }

    private fun doLogin() {
        activity?.startActivityForResult(Intent(activity, WebLoginActivity::class.java), getRequestCode())
    }

    private fun doOnLoginActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        intent?.data
            ?.takeIf { it.toString().startsWith(BrainheapProperties.redirectUri) }
            ?.let { intent.getStringExtra(WebLoginActivity.JSESSIONID) }
            ?.let { getUserEmail(it) }
            ?: onLoginFailed()
    }

    private fun getUserEmail(jSessionId: String) {
        HttpClientFactory.jSessionId = jSessionId
        CoroutineScope(Dispatchers.IO).launch {
            var toastMessage: String? = null
            try {
                val response = client.getCurrentUser().execute()
                require(response.isSuccessful) { "Get current user failed: ${response.code()}" }
                val email = response.body()
                require(email?.isNotEmpty() ?: false) { "Email is empty" }
                onLoginSuccess(
                    OAuthUserData(
                        email,
                        jSessionId
                    )
                )
            } catch (e: Throwable) {
                toastMessage = "Exception ${e.message}"
                onLoginFailed()
            }
            toastMessage?.let {
                withContext(Dispatchers.Main) {
                    Toast.makeText(BrainheapApp.applicationContext(), "Error: $it", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun getRequestCode(): Int = 9003
}