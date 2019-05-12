package com.brainheap.android.model

data class User(val name: String, val id: String, var email: String)
data class UserView(val name: String, val email: String)