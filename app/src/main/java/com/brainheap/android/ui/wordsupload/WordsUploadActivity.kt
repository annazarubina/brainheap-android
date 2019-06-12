package com.brainheap.android.ui.wordsupload

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.lifecycle.ViewModelProviders
import com.brainheap.android.R
import com.brainheap.android.login.LoginModule
import com.brainheap.android.preferences.CredentialsHolder
import com.brainheap.android.ui.login.LoginActivity

class WordsUploadActivity : AppCompatActivity() {
    private val loginModule = LoginModule(this)
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
        if (!loginModule.isLoggedIn()) {
            loginModule.logIn()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent()
    }

    private fun handleIntent() {
        intent?.let{
            when {
                it.action == Intent.ACTION_SEND -> {
                    it.getCharSequenceExtra(Intent.EXTRA_TEXT).toString()
                }
                it.action == Intent.ACTION_PROCESS_TEXT -> {
                    it.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString()
                }
                else -> {
                    it.extras?.getString("text")?:"Sample text"
                }
            }
        }?.let{text -> viewModel.init(text) }
    }
}
