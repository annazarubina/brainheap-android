package com.brainheap.android.ui.wordsupload

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

data class Word(val word: String, val start: Int, val end: Int, val picked: MutableLiveData<Boolean>)
data class WordsContext(val context: String, val wordList: List<Word>)

class WordsUploadViewModel : ViewModel() {
    val wordContext = MutableLiveData<WordsContext>()

    fun init(ctxStr:String) {
        if (wordContext.value?.context == ctxStr) return
        val wordList = ArrayList<Word>()

        val spaceList = ArrayList<Int>()
        spaceList.add(-1)
        ctxStr.foldIndexed(spaceList) { i, L, c->if (c==' '||c=='\n') L.add(i);L}
        spaceList.add(ctxStr.length)

        for (i in 1 until spaceList.size) {
            val s = spaceList[i - 1] + 1
            val e = spaceList[i]
            if (e - s > 2) {
                wordList.add(Word(ctxStr.substring(s, e),s,e, MutableLiveData(false)))
            }
        }
        wordContext.value = WordsContext(ctxStr,wordList)
    }
}
