package com.brainheap.android.ui.wordsupload

import android.graphics.Color
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
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import com.brainheap.android.R

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

    }

}
