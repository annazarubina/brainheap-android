package com.brainheap.android.login.data

import androidx.lifecycle.MutableLiveData
import com.brainheap.android.ui.login.OAuthUserData

class AuthProgressData {
    val oAuthData = MutableLiveData<OAuthUserData?>()
    val inProgress = MutableLiveData<Boolean>(false)

    fun start() {
        oAuthData.postValue(null)
        inProgress.postValue(true)
    }

    fun onSuccess(data: OAuthUserData) {
        oAuthData.postValue(data)
        inProgress.postValue(false)
    }

    fun onFailed() {
        oAuthData.postValue(null)
        inProgress.postValue(false)
    }

    fun clean() {
        oAuthData.postValue(null)
        inProgress.postValue(false)
    }
}