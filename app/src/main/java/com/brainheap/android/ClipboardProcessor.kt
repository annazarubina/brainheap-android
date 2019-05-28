package com.brainheap.android

import android.content.ClipboardManager
import android.content.Context
import com.brainheap.android.model.ItemView
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

class ClipboardProcessor(val context: Context) {
    private val clipboardManager: ClipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    fun process(): ItemView? {
        return clipboardManager.primaryClip
            ?.let {
                val array = ArrayList<String>()
                for (i: Int in 1..(clipboardManager.primaryClip?.itemCount ?: 0)) {
                    array.add(clipboardManager.primaryClip!!.getItemAt(i - 1).text.toString())
                }
                array
            }
            ?.filter { it.isNotEmpty() }
            ?.takeIf { it.isNotEmpty() }
            ?.let { array ->
                val title = array.first().split(" ").first()
                    .dropWhile { char -> !char.isLetterOrDigit() }
                    .dropLastWhile { char -> !char.isLetterOrDigit() }
                    .toLowerCase()
                val description = array.stream().collect(Collectors.joining(" "))
                ItemView(title, description)
            }
    }
}