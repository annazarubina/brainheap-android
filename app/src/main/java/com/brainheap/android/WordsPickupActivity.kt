package com.brainheap.android

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.widget.TextView
import android.widget.Toast
import android.text.style.ClickableSpan
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.view.View
import java.util.*
import android.text.TextPaint




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
        selectedTextView.movementMethod = LinkMovementMethod.getInstance()
        selectedTextView.highlightColor = resources.getColor(android.R.color.transparent,resources.newTheme())
        selectedTextView.setText(makeWordsClickable(text.toString()), TextView.BufferType.SPANNABLE)
    }

    private fun makeWordsClickable(str: String): SpannableStringBuilder {
        val ssb = SpannableStringBuilder(str)
        val spaceList = ArrayList<Int>()
        spaceList.add(-1)
        str.foldIndexed(spaceList) { i, L, c->if (c==' '||c=='\n') L.add(i);L}
        spaceList.add(str.length)

        for (i in 1 until spaceList.size) {
            val s = spaceList[i - 1] + 1
            val e = spaceList[i]
            if (e - s > 2) {
                ssb.setSpan(object : ClickableSpan() {
                    var picked: Boolean = false

                    override fun onClick(widget: View) {
                        picked = ! picked
                        widget.invalidate()
                        Toast.makeText(
                            this@WordsPickupActivity, str.substring(s, e),
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                     override  fun updateDrawState(ds: TextPaint) {
                        ds.color = if (picked) Color.RED else Color.GREEN
                        ds.isUnderlineText = false
                    }
                }, s, e, 0)
            }
        }

        return ssb
    }
}
