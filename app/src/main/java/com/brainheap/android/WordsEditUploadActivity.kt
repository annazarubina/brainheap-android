package com.brainheap.android

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.lifecycle.ViewModelProviders
import com.brainheap.android.ui.wordseditupload.WordsEditUploadFragment
import com.brainheap.android.ui.wordseditupload.WordsEditUploadViewModel

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
            getSharedPreferences()
        )
    }

    private fun getSharedPreferences(): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
}
