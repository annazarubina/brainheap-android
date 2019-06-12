package com.brainheap.android.preferences

import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.brainheap.android.BrainheapApp

object AppPreferences {
    private var preferences: SharedPreferences? = null

    fun get() =
        preferences ?: let {
            preferences = PreferenceManager.getDefaultSharedPreferences(BrainheapApp.applicationContext())
            preferences
        }!!
}