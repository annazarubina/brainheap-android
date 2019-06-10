package com.brainheap.android.preferences

import androidx.lifecycle.MutableLiveData

object CredentialsHolder {
    var email = MutableLiveData<String?>()
    var userId = MutableLiveData<String?>()

    fun clean() {
        email.postValue(null)
        userId.postValue(null)
    }
}