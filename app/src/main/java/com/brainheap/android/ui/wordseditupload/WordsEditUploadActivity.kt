package com.brainheap.android.ui.wordseditupload

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.brainheap.android.R

class WordsEditUploadActivity : AppCompatActivity() {

    private lateinit var viewModel: WordsEditUploadViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.words_upload_activity)
        viewModel = ViewModelProviders.of(this).get(WordsEditUploadViewModel::class.java)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, WordsEditUploadFragment.newInstance())
                .commitNow()
        }
        val bundle = intent.extras
        viewModel.init(
            bundle?.getString("title"),
            bundle?.getString("description"),
            bundle?.getString("translation"),
            bundle?.getString("itemId")
        )
    }
}
