package com.brainheap.android.login.authprovider.google

import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.brainheap.android.BrainheapApp
import com.brainheap.android.R
import com.brainheap.android.login.AuthProvider
import com.brainheap.android.login.data.AuthProgressData
import com.brainheap.android.login.data.OAuthUserData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class GoogleProvider(data: MutableLiveData<AuthProgressData>) : AuthProvider(data) {
    override fun doLogin() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity!!.getString(R.string.google_app_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(activity!!, googleSignInOptions)
        val signInIntent: Intent = googleSignInClient.signInIntent
        activity!!.startActivityForResult(signInIntent, getRequestCode())
    }

    override fun doOnLoginActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode == getRequestCode()) {
            onGoogleActivityResult(intent!!)
        }
    }

    private fun onGoogleActivityResult(intent: Intent) {
        var toastMessage: String? = null
        try {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)!!
            onLoginSuccess(
                OAuthUserData(
                    account.email,
                    account.idToken
                )
            )
        } catch (e: ApiException) {
            toastMessage = "Api exception. Error code: " + e.statusCode
            onLoginFailed()
        }
        toastMessage?.let {
            Toast.makeText(
                BrainheapApp.applicationContext(),
                "Google auth error: $it",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun getRequestCode(): Int = 9001
}