package com.brainheap.android.login.authprovider.facebook.client

import com.google.gson.annotations.SerializedName

data class FacebookUserInfo(
    @SerializedName("id") var id: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("email") var email: String? = null
)