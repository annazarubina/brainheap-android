package com.brainheap.android

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.widget.TextView



class WordsPickupActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_words_pickup)

        handleIntent()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent()
    }

    private fun handleIntent() {
        val selectedTextView:TextView = findViewById(R.id.selectedTextView)
        val text = intent
            .getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
        selectedTextView.text = text
    }
}
