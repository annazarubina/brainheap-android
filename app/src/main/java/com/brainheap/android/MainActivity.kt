package com.brainheap.android

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText

class MainActivity : AppCompatActivity() {

    val NAME_PROP = "name"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        val name = sharedPref.getString(NAME_PROP, "Unknown")
        val editText = findViewById<EditText>(R.id.editText)
        editText.setText(name)
    }

    fun saveName(view: View) {
        val sharedPref = getPreferences(Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            val editText = findViewById<EditText>(R.id.editText)
            putString(NAME_PROP, editText.text.toString())
            commit()
        }
    }
}
