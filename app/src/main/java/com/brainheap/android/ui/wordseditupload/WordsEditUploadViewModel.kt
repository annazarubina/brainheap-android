package com.brainheap.android.ui.wordseditupload

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.brainheap.android.network.RetrofitFactory
import com.brainheap.android.preferences.Constants.ID_PROP
import com.brainheap.android.preferences.Constants.SHOW_TRANSALTION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WordsEditUploadViewModel : ViewModel() {
    private val retrofitService = RetrofitFactory.makeRetrofitService()

    var itemId: String? = null

    val itemSaved = MutableLiveData<Boolean>(false)
    var title: String? = null
    var description: String? = null
    var sharedPreferences: SharedPreferences? = null

    var cashedDescription: String? = null

    val translation = MutableLiveData<String?>()

    fun init(
        titleString: String?, descriptionString: String?, translationString: String?,
        sharedPreferences: SharedPreferences, itemId: String?
    ) {
        this.sharedPreferences = sharedPreferences
        this.itemId = itemId
        title = titleString
        description = descriptionString
        translation.value = translationString
    }

    fun getUserId(): String? = sharedPreferences?.getString(ID_PROP, "")

    fun updateTranslation(description: String?) {
        val userId = getUserId()
        description
            ?.takeIf { it.isNotEmpty() }
            ?.takeIf { userId?.isNotEmpty() ?: false }
            ?.takeIf { cashedDescription?.let { it != description } ?: true }
            ?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val request = retrofitService
                            .translateAsync(userId!!, "\"" + description + "\"")
                        val response = request.await()
                        require(response.isSuccessful) { "Result: ${response.code()}" }
                        translation.postValue(response.body())
                    } catch (e: Throwable) {
                        "Translation error: ${e.message}"
                        translation.postValue("")
                    }
                }
            }
            ?: let { translation.postValue("") }
    }
}
