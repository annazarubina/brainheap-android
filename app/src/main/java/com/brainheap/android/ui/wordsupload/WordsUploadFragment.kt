package com.brainheap.android.ui.wordsupload

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import com.brainheap.android.R
import com.brainheap.android.ui.wordseditupload.WordsEditUploadActivity
import com.brainheap.android.model.ItemView
import com.brainheap.android.network.RetrofitFactory
import com.brainheap.android.ui.MainActivity
import com.facebook.FacebookSdk.getApplicationContext
import kotlinx.android.synthetic.main.words_upload_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class WordsUploadFragment : Fragment() {

    private val EDIT_WORDS_REQUEST = 1  // The request code

    companion object {
        val textColor = 0x80000000.toInt()
        val pickedTextColor = 0xFF82B1FF.toInt()
        fun newInstance() = WordsUploadFragment()
    }

    private lateinit var viewModel: WordsUploadViewModel
    private var selectedTextView: TextView? = null
    private var translatedTextView: TextView? = null
    private var showTranslatedTextCheckBox: CheckBox? = null

    private val retrofitService = RetrofitFactory.makeRetrofitService()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.words_upload_fragment, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initControls()

        viewModel.translatedText.observe(this, Observer<String> {
            translatedTextView?.text = it ?: ""
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
                            getApplicationContext(), word.word,
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

        viewModel.itemSaved.observe(this, Observer<Boolean> {
            if (it) activity!!.finish()
        })

        viewModel.showTranslatedText.observe(this, Observer<Boolean> {
            showTranslatedTextCheckBox?.isChecked = it
        })

        viewModel.translatedText.observe(this, Observer<String> {
            translatedTextView?.text = it
        })

        edit_button.setOnClickListener {
            val intent = Intent(this.context, WordsEditUploadActivity::class.java)
            intent.putExtra ("title", extractTitle())
            intent.putExtra ("description", viewModel.wordContext.value?.context )
            intent.putExtra ("translation", viewModel.translatedText.value )
            startActivityForResult(intent, EDIT_WORDS_REQUEST)
        }

        send_to_server_button.setOnClickListener {
            val wordsContext = viewModel.wordContext.value
            val translatedText = viewModel.translatedText.value
            if (viewModel.userId.value.isNullOrEmpty()) {
                Toast.makeText(getApplicationContext(), "User is not registered", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!wordsContext!!.wordList.any { it.pickedTime.value != null }) {
                Toast.makeText(getApplicationContext(), "Pick some words!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(getApplicationContext(), "Trying to create item", Toast.LENGTH_SHORT).show()

            CoroutineScope(Dispatchers.IO).launch {
                var toastMessage: String
                try {
                    val createItemRequest = retrofitService
                        .createItemAsync(
                            viewModel.userId.value!!,
                            ItemView( extractTitle()?:"", wordsContext.context + (translatedText?.let { " /// $translatedText" } ?: ""))
                        )
                    val createItemResponse = createItemRequest.await()
                    toastMessage = if (createItemResponse.isSuccessful) {
                        val itemId = createItemResponse.body()?.id
                        viewModel.itemSaved.postValue(true)
                        "Item created Id $itemId"
                    } else {
                        "CreateItem failed:${createItemResponse.code()}"
                    }

                } catch (e: HttpException) {
                    toastMessage = "Exception ${e.message}"

                } catch (e: Throwable) {
                    toastMessage = "Exception ${e.message}"
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
        show_translated_text_checkBox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setShowTranslatedText(isChecked)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == EDIT_WORDS_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                viewModel.itemSaved.postValue(true)
            }
        }
    }

    private fun extractTitle() : String? {
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

    private fun initControls() {
        selectedTextView = activity?.findViewById(R.id.selectedTextView1)
        translatedTextView = activity?.findViewById(R.id.translatedTextView)
        showTranslatedTextCheckBox = activity?.findViewById(R.id.show_translated_text_checkBox)
        selectedTextView?.movementMethod = LinkMovementMethod.getInstance()
        selectedTextView?.highlightColor = resources.getColor(android.R.color.transparent, resources.newTheme())
        activity?.let {
            viewModel = ViewModelProviders.of(it).get(WordsUploadViewModel::class.java)
        }
    }
}
