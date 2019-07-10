package com.brainheap.android.preferences

import androidx.lifecycle.MutableLiveData

object CredentialsHolder {
    var email = MutableLiveData<String?>()
    var userId = MutableLiveData<String?>()
    var jSessionId = MutableLiveData<String?>()
    var renewToken = MutableLiveData<String?>()

    fun clean() {
        email.postValue(null)
        userId.postValue(null)
        jSessionId.postValue(null)
        renewToken.postValue(null)
    }
}