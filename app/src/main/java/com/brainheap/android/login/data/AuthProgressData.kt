package com.brainheap.android.login.data

class OAuthUserData(val email: String?, val jSessionId: String?)

class AuthProgressData(
    val userId: String? = null,
    val email: String? = null,
    val jSessionId: String? = null,
    val inProgress: Boolean = false
)