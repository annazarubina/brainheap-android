package com.brainheap.android.ui.wordsupload

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.brainheap.android.BrainheapApp
import com.brainheap.android.R
import com.brainheap.android.model.ItemView
import com.brainheap.android.network.client.BrainheapClientFactory
import com.brainheap.android.network.client.QueueCallExecutor
import com.brainheap.android.ui.worddetail.HtmlTextBuilder
import com.brainheap.android.ui.wordseditupload.WordsEditUploadActivity
import kotlinx.android.synthetic.main.words_upload_fragment.*

class WordsUploadFragment : Fragment() {

    private val EDIT_WORDS_REQUEST = 1  // The request code

    companion object {
        val textColor = 0x80000000.toInt()
        val pickedTextColor = 0xFF82B1FF.toInt()
        fun newInstance() = WordsUploadFragment()
    }

    private lateinit var viewModel: WordsUploadViewModel
    private val retrofitService = BrainheapClientFactory.get()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.words_upload_fragment, container, false)

    private fun initControls() {
        selectedTextView?.movementMethod = LinkMovementMethod.getInstance()
        selectedTextView?.highlightColor = resources.getColor(android.R.color.transparent, resources.newTheme())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            viewModel = ViewModelProviders.of(it).get(WordsUploadViewModel::class.java)
        }
        initControls()

        viewModel.showTranslation.observe(this, Observer<Boolean> {
            show_translated_text_checkBox?.isChecked = it
            viewModel.loadTranslation()
        })

        viewModel.translation.observe(this, Observer<String> {
            translatedTextView?.text = it
        })

        viewModel.itemSaved.observe(this, Observer<Boolean> { saved ->
            if (saved) {
                viewModel.save()
                activity!!.setResult(Activity.RESULT_OK)
                activity!!.finish()
            }
        })

        viewModel.wordContext.observe(this, Observer<WordsContext> { wordsContext ->
            val ssb = SpannableStringBuilder(wordsContext.context)
            for (word in wordsContext.wordList) {
                val span = object : ClickableSpan() {
                    var pickedTime: Long? = null
                    override fun onClick(widget: View) {
                        if (word.pickedTime.value == null) {
                            word.pickedTime.value = System.currentTimeMillis()
                        } else {
                            word.pickedTime.value = null
                        }
                        widget.invalidate()
                        Toast.makeText(
                            BrainheapApp.applicationContext(), word.word,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        ds.color = if (pickedTime != null) pickedTextColor else textColor
                    }
                }
                word.pickedTime.observe(this, Observer { pickedTime ->
                    span.pickedTime = pickedTime
                })
                ssb.setSpan(span, word.start, word.end, 0)
            }
            selectedTextView?.setText(ssb, TextView.BufferType.SPANNABLE)
        })

        edit_button.setOnClickListener {
            val intent = Intent(this.context, WordsEditUploadActivity::class.java)
            intent.putExtra("title", extractTitle())
            intent.putExtra("description", viewModel.wordContext.value?.context)
            intent.putExtra("translation", viewModel.translation.value)
            startActivityForResult(intent, EDIT_WORDS_REQUEST)
        }

        send_to_server_button.setOnClickListener {
            val userId = viewModel.userId
            val wordsContext = viewModel.wordContext.value
            val translation = viewModel.translation.value
            if (userId.isNullOrEmpty()) {
                Toast.makeText(BrainheapApp.applicationContext(), "User is not registered", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!wordsContext!!.wordList.any { it.pickedTime.value != null }) {
                Toast.makeText(BrainheapApp.applicationContext(), "Pick some words!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(BrainheapApp.applicationContext(), "Trying to create item", Toast.LENGTH_SHORT).show()
            QueueCallExecutor.add(
                QueueCallExecutor.Data(
                    retrofitService.createItem(
                        userId,
                        ItemView(
                            extractTitle() ?: "",
                            HtmlTextBuilder.joinDescription(wordsContext.context, translation) ?: ""
                        )
                    ),
                    object : QueueCallExecutor.Callback {
                        override fun onSuccess() {}
                        override fun onError(message: String) {}
                    }
                )
            )
            viewModel.itemSaved.postValue(true)
        }
        show_translated_text_checkBox.setOnCheckedChangeListener { _, checked ->
            viewModel.showTranslation.postValue(checked)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == EDIT_WORDS_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                viewModel.itemSaved.postValue(true)
            }
        }
    }

    private fun extractTitle(): String? {
        return viewModel.wordContext.value
            ?.wordList
            ?.filter { it.pickedTime.value != null }
            ?.sortedBy { it.pickedTime.value }
            ?.map { word ->
                word.word
                    .dropWhile { !it.isLetterOrDigit() }
                    .dropLastWhile { !it.isLetterOrDigit() }
                    .toLowerCase()
            }
            ?.joinToString(" ") { it }
    }
}
