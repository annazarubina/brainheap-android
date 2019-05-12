package com.brainheap.android.ui.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel:ViewModel() {
    val loginSuccess =  MutableLiveData<Boolean>(false)
}