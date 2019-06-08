package com.brainheap.android.ui

import android.content.ClipboardManager
import android.content.Context
import java.util.stream.Collectors
import kotlin.collections.ArrayList

class ClipboardProcessor(val context: Context) {
    private val clipboardManager: ClipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    fun process(): String? {
        return clipboardManager.primaryClip
            ?.let {
                val array = ArrayList<String>()
                for (i: Int in 1..(clipboardManager.primaryClip?.itemCount ?: 0)) {
                    array.add(clipboardManager.primaryClip!!.getItemAt(i - 1).text.toString())
                }
                array.stream().collect(Collectors.joining(" "))
            }
            ?.dropWhile { char -> !char.isLetterOrDigit() }
            ?.dropLastWhile { char -> !char.isLetterOrDigit() }
            ?.takeIf { it.isNotEmpty() }
    }
}