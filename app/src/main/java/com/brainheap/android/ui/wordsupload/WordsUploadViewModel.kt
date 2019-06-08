package com.brainheap.android.ui.wordsupload

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brainheap.android.preferences.Constants.ID_PROP
import com.brainheap.android.preferences.Constants.SHOW_TRANSALTION
import com.brainheap.android.network.RetrofitFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

data class Word(val word: String, val start: Int, val end: Int, var pickedTime: MutableLiveData<Long?>)
data class WordsContext(val context: String, val wordList: List<Word>)

class WordsUploadViewModel : ViewModel() {
    private val retrofitService = RetrofitFactory.makeRetrofitService()

    val userId = MutableLiveData<String>()
    val wordContext = MutableLiveData<WordsContext>()
    val itemSaved = MutableLiveData<Boolean>(false)
    val showTranslatedText = MutableLiveData<Boolean>(true)
    val translatedText = MutableLiveData<String>()
    private var cachedTranslatedText: String? = null
    private var sharedPreferences: SharedPreferences? = null

    fun init(ctxStr: String, sharedPreferences: SharedPreferences) {
        if (wordContext.value?.context == ctxStr) return
        this.sharedPreferences = sharedPreferences
        userId.value = sharedPreferences.getString(ID_PROP, "")
        showTranslatedText.value = sharedPreferences.getBoolean(SHOW_TRANSALTION, true)
        translatedText.value = ""

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
        updateTranslatedText()
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

    private fun updateTranslatedText() {
        if (!wordContext.value?.context.isNullOrEmpty() && !userId.value.isNullOrEmpty() && showTranslatedText.value == true) {
            cachedTranslatedText
                ?.let { translatedText.postValue(cachedTranslatedText) }
                ?: (CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val request = retrofitService
                            .translateAsync(userId.value!!,"\"" + wordContext.value?.context!! + "\"")
                        val response = request.await()
                        if (response.isSuccessful) {
                            cachedTranslatedText = response.body()
                            translatedText.postValue(response.body())
                        } else {
                            translatedText.postValue("")
                        }
                    } catch (e: Throwable) {
                        "Error: ${e.message}"
                        translatedText.postValue("")
                    }
                })
        } else {
            translatedText.postValue("")
        }
    }

    @SuppressLint("CommitPrefEdits")
    fun setShowTranslatedText(value: Boolean) {
        if (showTranslatedText.value?.equals(value) == true) return
        sharedPreferences!!.edit().putBoolean(SHOW_TRANSALTION, value).apply()
        showTranslatedText.value = value
        updateTranslatedText()
    }
}
