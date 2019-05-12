package com.brainheap.android.model

data class Item(val created: String,
                val description: String,
                val id:	String,
                val modified: String,
                val title: String,
                val userId:	String)

data class ItemView(val title: String, val description: String)