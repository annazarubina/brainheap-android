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
import kotlinx.android.synthetic.main.words_upload_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class WordsUploadFragment : Fragment() {

    companion object {
        fun newInstance() = WordsUploadFragment()
    }

    private lateinit var viewModel: WordsUploadViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.words_upload_fragment, container, false)
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val selectedTextView: TextView? = activity?.findViewById(R.id.selectedTextView1)
        selectedTextView?.movementMethod = LinkMovementMethod.getInstance()
        selectedTextView?.highlightColor = resources.getColor(android.R.color.transparent,resources.newTheme())
        activity?.let {
            viewModel = ViewModelProviders.of(it).get(WordsUploadViewModel::class.java)
        }
        viewModel.wordContext.observe(this, Observer <WordsContext>{wordsContext->
            val ssb = SpannableStringBuilder(wordsContext.context)
            for (word in wordsContext.wordList) {
                val span = object : ClickableSpan() {
                    var picked = false
                    override fun onClick(widget: View) {
                        word.picked.value =  word.picked.value?.not()
                        widget.invalidate()
                        Toast.makeText(
                            activity, word.word,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    override  fun updateDrawState(ds: TextPaint) {
                        ds.color = if (picked) Color.RED else Color.GREEN
                    }
                }
                word.picked.observe(this, Observer <Boolean>{picked->
                    span.picked = picked
                })
                ssb.setSpan(span, word.start, word.end, 0)
            }
            selectedTextView?.setText(ssb,TextView.BufferType.SPANNABLE)
        })

        viewModel.itemSaved.observe(this, Observer <Boolean>{
            if (it) activity!!.finish()
        })

        send_to_server_button.setOnClickListener {
            val wordsContext = viewModel.wordContext.value
            val sharedPref = PreferenceManager.getDefaultSharedPreferences(activity)
            val userId = sharedPref.getString(ID_PROP, "")
            if (userId.isNullOrEmpty()) {
                Toast.makeText(activity!!.applicationContext, "User is not registered", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!wordsContext!!.wordList.any {it.picked.value!!}){
                Toast.makeText(activity!!.applicationContext, "Pick some words!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(activity!!.applicationContext, "Trying to create item", Toast.LENGTH_SHORT).show()

            CoroutineScope(Dispatchers.IO).launch {
                val retrofitService = RetrofitFactory.makeRetrofitService()
                var toastMessage: String
                try {
                    val createItemRequest = retrofitService
                        .createItemAsync(
                            userId,
                            ItemView(
                                wordsContext.wordList
                                    .filter { it.picked.value!! }
                                    .joinToString(" ") { it.word },
                                wordsContext.context)
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

    }

}
