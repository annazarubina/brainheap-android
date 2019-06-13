package com.brainheap.android.login

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.brainheap.android.login.data.AuthProgressData
import com.brainheap.android.login.data.OAuthUserData
import com.brainheap.android.model.UserView
import com.brainheap.android.network.client.BrainheapClientFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

abstract class AuthProvider(val data: MutableLiveData<AuthProgressData>) {
    enum class Type(val string: String) {
        GOOGLE("GOOGLE"),
        FACEBOOK("FACEBOOK"),
        KEYCLOAK_SERVER("KEYCLOAK_SERVER")
    }

    protected var activity: Activity? = null

    fun login(activity: Activity) {
        clean()
        this.activity = activity
        start()
        doLogin()
    }

    fun loginByEmailOnly(email: String?, activity: Activity) {
        clean()
        this.activity = activity
        start()
        loadUserId(OAuthUserData(email, null))
    }

    fun logout() {
        clean()
    }

    fun onLoginActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        doOnLoginActivityResult(requestCode, resultCode, intent)
    }

    protected abstract fun doLogin()
    protected abstract fun doOnLoginActivityResult(requestCode: Int, resultCode: Int, intent: Intent?)

    abstract fun getRequestCode(): Int

    protected fun onLoginSuccess(value: OAuthUserData) {
        loadUserId(value)
    }

    protected fun onLoginFailed() {
        onFailed()
    }

    private fun loadUserId(value: OAuthUserData?) {
        value?.email?.let { email ->
            CoroutineScope(Dispatchers.IO).launch {
                var userId: String? = null
                val retrofitService = BrainheapClientFactory.get()
                var toastMessage: String? = null
                try {
                    val findUserRequest = retrofitService.findUserAsync(email)
                    val findUserResponse = findUserRequest.await()
                    if (findUserResponse.isSuccessful) {
                        userId = findUserResponse.body()?.firstOrNull()?.id
                    } else if (findUserResponse.code() == 404) {
                        val createUserRequest = retrofitService.createUserAsync(UserView(email, email))
                        val createUserResponse = createUserRequest.await()
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
        data.postValue(AuthProgressData(userId, oAuthData.email, oAuthData.token, false))
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
}