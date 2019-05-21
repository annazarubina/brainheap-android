package com.brainheap.android.ui.wordseditupload

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brainheap.android.Constants.ID_PROP
import com.brainheap.android.Constants.SHOW_TRANSALTION
import com.brainheap.android.network.RetrofitFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class WordsEditUploadViewModel : ViewModel() {
    private val retrofitService = RetrofitFactory.makeRetrofitService()

    val itemSaved = MutableLiveData<Boolean>(false)
    val showTranslation = MutableLiveData<Boolean>(true)
    val titleText = MutableLiveData<String>()
    val descriptionText = MutableLiveData<String>()
    val translationText = MutableLiveData<String>()
    var sharedPreferences: SharedPreferences? = null

    fun init(titleString: String?, descriptionString: String?, translationString: String?, sharedPreferences: SharedPreferences) {
        this.sharedPreferences = sharedPreferences
        showTranslation.value = sharedPreferences.getBoolean(SHOW_TRANSALTION, true)
        titleText.value = titleString
        descriptionText.value = descriptionString
        translationText.value = translationString
        updateTranslatedText()
    }

    fun getUserId() : String? = sharedPreferences?.getString(ID_PROP, "")

    private fun updateTranslatedText() {
        val userId = getUserId()
        if (!descriptionText.value.isNullOrEmpty() && !userId.isNullOrEmpty() && showTranslation.value == true) {
            CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val request = retrofitService
                            .translateAsync(userId,"\"" + descriptionText.value!! + "\"")
                        val response = request.await()
                        if (response.isSuccessful) {
                            translationText.postValue(response.body())
                        } else {
                            translationText.postValue("")
                        }
                    } catch (e: Throwable) {
                        "Error: ${e.message}"
                        translationText.postValue("")
                    }
                }
        } else {
            translationText.postValue("")
        }
    }

    @SuppressLint("CommitPrefEdits")
    fun setShowTranslatedText(value: Boolean) {
        if (showTranslation.value?.equals(value) == true) return
        sharedPreferences!!.edit().putBoolean(SHOW_TRANSALTION, value).apply()
        showTranslation.value = value
        updateTranslatedText()
    }
}
