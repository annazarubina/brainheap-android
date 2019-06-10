package com.brainheap.android.ui.wordseditupload

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.brainheap.android.BrainheapApp
import com.brainheap.android.R
import com.brainheap.android.model.ItemView
import com.brainheap.android.network.RetrofitFactory
import com.brainheap.android.preferences.Constants
import kotlinx.android.synthetic.main.words_edit_upload_fragment.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class WordsEditUploadFragment : Fragment() {
    companion object {
        fun newInstance() = WordsEditUploadFragment()
    }

    private lateinit var viewModel: WordsEditUploadViewModel
    private val retrofitService = RetrofitFactory.makeRetrofitService()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.words_edit_upload_fragment, container, false)
    }

    private fun initControls() {
        titleEditText?.setText(viewModel.title ?: "")
        descriptionEditText?.setText(viewModel.description ?: "")
        translatedEditText?.setText(viewModel.translation.value ?: "")

        edit_show_translated_text_checkBox?.isChecked =
            viewModel.sharedPreferences
                ?.getBoolean(Constants.SHOW_TRANSALTION, true)
                ?: false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            viewModel = ViewModelProviders.of(it).get(WordsEditUploadViewModel::class.java)
        }
        initControls()

        viewModel.translation.observe(this, Observer {
            translatedEditText?.setText(it ?: "")
        }
        )

        viewModel.itemSaved.observe(this, Observer<Boolean> { saved ->
            if (saved) {
                edit_show_translated_text_checkBox?.let {
                    viewModel.sharedPreferences?.edit()
                        ?.putBoolean(Constants.SHOW_TRANSALTION, it.isChecked)?.apply()
                }
                activity!!.setResult(Activity.RESULT_OK)
                activity!!.finish()
            }
        })

    edit_send_to_server_button.setOnClickListener {
        val userId = viewModel.getUserId()
        val title = titleEditText?.editableText.toString()
        val description = descriptionEditText?.editableText.toString()
        val translation = translatedEditText?.editableText.toString()
        if (userId.isNullOrEmpty()) {
            Toast.makeText(BrainheapApp.applicationContext(), "User is not registered", Toast.LENGTH_SHORT).show()
            return@setOnClickListener
        }
        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(
                BrainheapApp.applicationContext(),
                "Pick some words for title and description",
                Toast.LENGTH_SHORT
            ).show()
            return@setOnClickListener
        }
        Toast.makeText(BrainheapApp.applicationContext(), "Trying to create item", Toast.LENGTH_SHORT).show()

        CoroutineScope(Dispatchers.IO).launch {
            var toastMessage: String
            try {
                val createItemRequest = viewModel.itemId?.takeIf { it.isNotEmpty() }?.let {
                    retrofitService
                        .updateItemAsync(
                            userId,
                            it,
                            ItemView(title, description + translation.let { " /// $translation" })
                        )
                } ?: let {
                    retrofitService
                        .createItemAsync(
                            userId,
                            ItemView(title, description + translation.let { " /// $translation" })
                        )
                }
                val createItemResponse = createItemRequest.await()
                toastMessage = if (createItemResponse.isSuccessful) {
                    val itemId = createItemResponse.body()?.id
                    viewModel.itemSaved.postValue(true)
                    "Item created Id $itemId"
                } else {
                    "CreateItem failed:${createItemResponse.code()}"
                }

            } catch (e: HttpException) {
                toastMessage = "Exception ${e.message}"

            } catch (e: Throwable) {
                toastMessage = "Exception ${e.message}"
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(BrainheapApp.applicationContext(), toastMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

     descriptionEditText.setOnFocusChangeListener{ _, hasFocus ->
         if(!hasFocus && edit_show_translated_text_checkBox.isChecked) {
             viewModel.updateTranslation(descriptionEditText?.editableText.toString())
         }
     }

    edit_show_translated_text_checkBox.setOnCheckedChangeListener { _, it ->
        when (it) {
            true -> {
                translatedEditText.visibility = View.VISIBLE
                viewModel.updateTranslation(descriptionEditText?.editableText.toString())
            }
            false -> translatedEditText.visibility = View.GONE
        }
    }
}
}