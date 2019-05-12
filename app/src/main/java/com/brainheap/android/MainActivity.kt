package com.brainheap.android

import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import com.brainheap.android.Constants.NAME_PROP

class MainActivity : AppCompatActivity()   {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //val sharedPref = PreferenceManager.getDefaultSharedPreferences(this) ?: return
        //val name = sharedPref.getString(NAME_PROP, "Unknown")

    }



}
