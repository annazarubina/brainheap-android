package com.brainheap.android.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OAuthUserData(var email: String?, var token: String?)

class LoginViewModel:ViewModel() {
    val loginSuccess =  MutableLiveData<Boolean>(false)
    val email =  MutableLiveData<String?>()
    val userId =  MutableLiveData<String?>()
}