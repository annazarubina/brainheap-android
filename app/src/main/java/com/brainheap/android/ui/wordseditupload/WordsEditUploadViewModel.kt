package com.brainheap.android.ui.wordseditupload

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brainheap.android.network.client.BrainheapClientFactory
import com.brainheap.android.preferences.AppPreferences
import com.brainheap.android.preferences.Constants
import com.brainheap.android.preferences.Constants.ID_PROP
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WordsEditUploadViewModel : ViewModel() {
    private val retrofitService = BrainheapClientFactory.get()

    var itemId: String? = null
    var userId: String? = null
    val itemSaved = MutableLiveData<Boolean>(false)

    var title: String? = null
    var description: String? = null
    val translation = MutableLiveData<String?>()
    val showTranslation = MutableLiveData<Boolean>(true)

    var cashedDescription: String? = null
    var cashedTranslation: String? = null

    fun init(title: String?, description: String?, translation: String?, itemId: String?) {
        this.itemId = itemId
        this.userId = AppPreferences.get().getString(ID_PROP, "")
        this.showTranslation.postValue(translation?.isNotEmpty())
        this.translation.postValue(translation)
        this.title = title
        this.description = description

        this.cashedDescription = description
        this.cashedTranslation = translation
    }

    @SuppressLint("CommitPrefEdits")
    fun save() {
        AppPreferences.get().edit().putBoolean(Constants.SHOW_TRANSALTION, showTranslation.value ?: true).apply()
    }

    fun loadTranslation(description: String?) {
        description
            ?.takeIf { it.isNotEmpty() }
            ?.takeIf { userId?.isNotEmpty() ?: false }
            ?.takeIf { showTranslation.value == true }
            ?.let {
                if (cashedDescription != description || cashedTranslation.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        cashedTranslation = try {
                            val request = retrofitService
                                .translateAsync(userId!!, "\"" + it + "\"")
                            val response = request.await()
                            require(response.isSuccessful) { "Result: ${response.code()}" }
                            cashedDescription = description
                            response.body()
                        } catch (e: Throwable) {
                            ""
                        }
                        translation.postValue(cashedTranslation)
                    }
                } else {
                    translation.postValue(cashedTranslation)
                }
            } ?: let { translation.postValue("") }
    }
}
