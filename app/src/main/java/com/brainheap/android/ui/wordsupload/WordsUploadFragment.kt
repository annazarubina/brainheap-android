package com.brainheap.android.ui.wordsupload

import android.content.Context
import android.graphics.Color
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import com.brainheap.android.Constants.ID_PROP
import com.brainheap.android.Constants.NAME_PROP
import com.brainheap.android.R
import com.brainheap.android.model.ItemView
import com.brainheap.android.model.UserView
import com.brainheap.android.network.RetrofitFactory
import com.google.common.net.UrlEscapers
import kotlinx.android.synthetic.main.words_upload_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.URLEncoder
import java.util.*

class WordsUploadFragment : Fragment() {

    companion object {
        val textColor = 0xFF0D47A1.toInt()
        val pickedTextColor = 0xFF82B1FF.toInt()
        fun newInstance() = WordsUploadFragment()
    }

    private lateinit var viewModel: WordsUploadViewModel
    private var selectedTextView: TextView? = null
    private var translatedTextView: TextView? = null

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
            translatedTextView?.text = it?:""
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
                            activity, word.word,
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

        send_to_server_button.setOnClickListener {
            val wordsContext = viewModel.wordContext.value
            val translatedText = viewModel.translatedText.value
            val userId = getUserId()
            if (userId.isNullOrEmpty()) {
                Toast.makeText(activity!!.applicationContext, "User is not registered", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!wordsContext!!.wordList.any { it.pickedTime.value != null }) {
                Toast.makeText(activity!!.applicationContext, "Pick some words!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(activity!!.applicationContext, "Trying to create item", Toast.LENGTH_SHORT).show()

            CoroutineScope(Dispatchers.IO).launch {
                var toastMessage: String
                try {
                    val createItemRequest = retrofitService
                        .createItemAsync(
                            userId,
                            ItemView(
                                wordsContext.wordList
                                    .filter { it.pickedTime.value != null }
                                    .sortedBy { it.pickedTime.value }
                                    .map { word ->
                                        word.word
                                            .dropWhile { !it.isLetterOrDigit() }
                                            .dropLastWhile { !it.isLetterOrDigit() }
                                            .toLowerCase()
                                    }
                                    .joinToString(" ") { it },
                                wordsContext.context + translatedText?.let { "/// $translatedText" }
                            )
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
                    Toast.makeText(activity!!.applicationContext, toastMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
        translatedTextView?.text = viewModel.translatedText.value
    }

    private fun initControls() {
        selectedTextView = activity?.findViewById(R.id.selectedTextView1)
        translatedTextView = activity?.findViewById(R.id.translatedTextView)
        selectedTextView?.movementMethod = LinkMovementMethod.getInstance()
        selectedTextView?.highlightColor = resources.getColor(android.R.color.transparent, resources.newTheme())
        activity?.let {
            viewModel = ViewModelProviders.of(it).get(WordsUploadViewModel::class.java)
        }
    }

    private fun getUserId(): String? = PreferenceManager.getDefaultSharedPreferences(activity).getString(ID_PROP, "")

}
