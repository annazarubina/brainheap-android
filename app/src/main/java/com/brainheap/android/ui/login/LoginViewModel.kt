package com.brainheap.android.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OAuthData(var userId: String?, var token: String?)
class OAuthUserData{
    var id: String? = null
    var name: String? = null
    var email: String? = null
}

class LoginViewModel:ViewModel() {
    val loginSuccess =  MutableLiveData<Boolean>(false)
    val oAuthData =  MutableLiveData<OAuthData?>()
    val email =  MutableLiveData<String?>()
    val userId =  MutableLiveData<String?>()
}