package com.brainheap.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import com.brainheap.android.ui.wordsupload.WordsUploadFragment
import com.brainheap.android.ui.wordsupload.WordsUploadViewModel

class WordsUploadActivity : AppCompatActivity() {

    private lateinit var viewModel: WordsUploadViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.words_upload_activity)
        viewModel = ViewModelProviders.of(this).get(WordsUploadViewModel::class.java)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, WordsUploadFragment.newInstance())
                .commitNow()
        }
        handleIntent()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent()
    }

    private fun handleIntent() {
        val text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
        viewModel.init(text.toString())
    }

}
