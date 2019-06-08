package com.brainheap.android.login.authprovider.google

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.brainheap.android.R
import com.brainheap.android.login.AuthProvider
import com.brainheap.android.login.data.AuthProgressData
import com.brainheap.android.ui.login.OAuthUserData
import com.facebook.FacebookSdk.getApplicationContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class GoogleProvider(data: AuthProgressData) : AuthProvider(data) {
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
            data.onSuccess(
                OAuthUserData(
                    account.email,
                    account.idToken
                )
            )
        } catch (e: ApiException) {
            toastMessage = "Api exception. Error code: " + e.statusCode
            data.onFailed()
        }
        toastMessage?.let { Toast.makeText(getApplicationContext(), "Google auth error: $it", Toast.LENGTH_SHORT).show() }
    }

    override fun logout() {}

    override fun getRequestCode(): Int = 9001
}