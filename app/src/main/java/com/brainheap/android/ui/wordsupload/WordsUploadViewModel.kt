package com.brainheap.android.ui.wordsupload

import android.preference.PreferenceManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brainheap.android.Constants
import com.brainheap.android.network.RetrofitFactory
import com.google.common.net.UrlEscapers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

data class Word(val word: String, val start: Int, val end: Int, var pickedTime: MutableLiveData<Long?>)
data class WordsContext(val context: String, val wordList: List<Word>)

class WordsUploadViewModel : ViewModel() {
    private val retrofitService = RetrofitFactory.makeRetrofitService()

    val wordContext = MutableLiveData<WordsContext>()
    val itemSaved = MutableLiveData<Boolean>(false)
    val translatedText = MutableLiveData<String>()

    fun init(userId: String?, ctxStr: String) {
        if (wordContext.value?.context == ctxStr) return
        val wordList = ArrayList<Word>()
        val spaceList = ArrayList<Int>()
        spaceList.add(-1)
        ctxStr.foldIndexed(spaceList) { i, L, c -> if (c == ' ' || c == '\n') L.add(i);L }
        spaceList.add(ctxStr.length)

        for (i in 1 until spaceList.size) {
            val s = spaceList[i - 1] + 1
            val e = spaceList[i]
            if (e - s > 2) {
                wordList.add(Word(ctxStr.substring(s, e), s, e, MutableLiveData(null)))
            }
        }
        wordContext.value = WordsContext(ctxStr, wordList)
        if (!ctxStr.isNullOrEmpty() && !userId.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = retrofitService
                        .translateAsync(userId, "\"" + ctxStr + "\"")
                    val response = request.await()
                    if (response.isSuccessful) {
                        translatedText.postValue(response.body())

                    }
                } catch (e: Throwable) {
                    "Error: ${e.message}"
                }
            }
        }
    }

    private fun setWordContext(ctxStr: String) {
        val wordList = ArrayList<Word>()

        val spaceList = ArrayList<Int>()
        spaceList.add(-1)
        ctxStr.foldIndexed(spaceList) { i, L, c -> if (c == ' ' || c == '\n') L.add(i);L }
        spaceList.add(ctxStr.length)

        for (i in 1 until spaceList.size) {
            val s = spaceList[i - 1] + 1
            val e = spaceList[i]
            if (e - s > 2) {
                wordList.add(Word(ctxStr.substring(s, e), s, e, MutableLiveData(null)))
            }
        }
    }

    private fun setTranslatedText(userId: String?, ctxStr: String) {
        if (!ctxStr.isNullOrEmpty() && !userId.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = retrofitService
                        .translateAsync(userId, UrlEscapers.urlFragmentEscaper().escape(ctxStr))
                    val response = request.await()
                    if (response.isSuccessful) {
                        translatedText.value = response.body()

                    }
                } catch (e: Throwable) {
                    "Error: ${e.message}"
                }
            }
        }
    }
}
