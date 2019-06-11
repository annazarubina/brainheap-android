package com.brainheap.android.login.data

class OAuthUserData(val email: String?, val token: String?)

class AuthProgressData(
    val userId: String? = null,
    val email: String? = null,
    val token: String? = null,
    val inProgress: Boolean = false
)