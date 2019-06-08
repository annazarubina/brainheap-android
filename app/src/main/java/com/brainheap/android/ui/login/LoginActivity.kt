package com.brainheap.android.ui.login

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.brainheap.android.R
import com.brainheap.android.login.AuthProvider
import com.brainheap.android.login.AuthProviderManager
import com.brainheap.android.model.UserView
import com.brainheap.android.network.RetrofitFactory
import com.brainheap.android.preferences.Constants
import com.brainheap.android.preferences.CredentialsHolder
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException


class LoginActivity : AppCompatActivity() {
    private lateinit var viewModel: LoginViewModel
    private val providerManager = AuthProviderManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_login)
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)

        keycloak_login_button.setOnClickListener {
            providerManager.login(AuthProvider.Type.KEYCLOAK_SERVER, this)
        }
        fb_login_button.setOnClickListener {
            providerManager.login(AuthProvider.Type.FACEBOOK, this)
        }
        google_login_button.setOnClickListener {
            providerManager.login(AuthProvider.Type.GOOGLE, this)
        }

        viewModel.loginSuccess.observe(this, Observer { loginSuccess ->
            loginSuccess.takeIf { it == true }
                ?.let { finish() }
        })

        providerManager.data.inProgress.observe(this, Observer { inProgress ->
            if(inProgress) {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                loadingSpinner.visibility = View.VISIBLE
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                providerManager.data.oAuthData.value?.let { getUserId(it.email) }
                loadingSpinner.visibility = View.GONE
            }
        })

        viewModel.email.observe(this, Observer {
            getUserId(it)
        })

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val mail = sharedPref.getString(Constants.NAME_PROP, "Unknown")
        emailEditText.setText(mail)

        viewModel.userId.observe(this, Observer {
            CredentialsHolder.userId = it
            sharedPref.edit().putString(Constants.ID_PROP, it).apply()
            viewModel.loginSuccess.postValue(true)
        })

        emailSetButton.setOnClickListener {
            Toast.makeText(applicationContext, "Trying to fetch user data", Toast.LENGTH_SHORT).show()
            viewModel.email.postValue(emailEditText.text.toString())
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        providerManager.get().onLoginActivityResult(0, 0, intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        providerManager.get().onLoginActivityResult(requestCode, resultCode, intent)
    }

    override fun onBackPressed() {}

    private fun getUserId(email: String?) {
        email?.let {
            CoroutineScope(Dispatchers.IO).launch {
                var userId: String? = null
                val retrofitService = RetrofitFactory.makeRetrofitService()
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
                        Toast.makeText(applicationContext, "Error: $it", Toast.LENGTH_SHORT).show()
                    }
                }
                userId?.let { viewModel.userId.postValue(it) }
            }
        }
    }
}
