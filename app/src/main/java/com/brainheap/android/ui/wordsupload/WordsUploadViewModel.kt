package com.brainheap.android.ui.wordsupload

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brainheap.android.network.client.BrainheapClientFactory
import com.brainheap.android.preferences.AppPreferences
import com.brainheap.android.preferences.Constants.ID_PROP
import com.brainheap.android.preferences.Constants.SHOW_TRANSALTION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

data class Word(val word: String, val start: Int, val end: Int, var pickedTime: MutableLiveData<Long?>)
data class WordsContext(val context: String, val wordList: List<Word>)

class WordsUploadViewModel : ViewModel() {
    private val retrofitService = BrainheapClientFactory.get()

    var userId: String? = null
    val itemSaved = MutableLiveData<Boolean>(false)

    val wordContext = MutableLiveData<WordsContext>()
    val translation = MutableLiveData<String>()
    val showTranslation = MutableLiveData<Boolean>(true)
    private var cashedTranslation: String? = null

    fun init(str: String) {
        if (wordContext.value?.context == str) return
        this.userId = AppPreferences.get().getString(ID_PROP, "")
        this.showTranslation.value = AppPreferences.get().getBoolean(SHOW_TRANSALTION, true)
        this.translation.value = ""
        this.wordContext.value = createWordsContext(str)
        loadTranslation()
    }

    @SuppressLint("CommitPrefEdits")
    fun save() {
        AppPreferences.get().edit().putBoolean(SHOW_TRANSALTION, showTranslation.value ?: true).apply()
    }

    private fun createWordsContext(src: String): WordsContext {
        val wordList = ArrayList<Word>()
        val spaceList = ArrayList<Int>()
        spaceList.add(-1)
        src.foldIndexed(spaceList) { i, L, c -> if (c == ' ' || c == '\n') L.add(i);L }
        spaceList.add(src.length)

        for (i in 1 until spaceList.size) {
            val s = spaceList[i - 1] + 1
            val e = spaceList[i]
            if (e - s > 2) {
                wordList.add(Word(src.substring(s, e), s, e, MutableLiveData(null)))
            }
        }
        return WordsContext(src, wordList)
    }

    fun loadTranslation() {
        wordContext.value?.context
            ?.takeIf { it.isNotEmpty() }
            ?.takeIf { userId?.isNotEmpty() ?: false }
            ?.takeIf { showTranslation.value == true }
            ?.let {
                if (cashedTranslation.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        cashedTranslation = try {
                            val request = retrofitService
                                .translateAsync(userId!!, "\"" + it + "\"")
                            val response = request.await()
                            require(response.isSuccessful) { "Result: ${response.code()}" }
                            response.body()
                        } catch (e: Throwable) {
                            ""
                        }
                        translation.postValue(cashedTranslation)
                    }
                } else {
                    translation.postValue(cashedTranslation)
                }
            }
            ?: let { translation.postValue("") }
    }
}
