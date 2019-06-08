package com.brainheap.android.ui.wordseditupload

import android.app.Activity
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Observer
import com.brainheap.android.R
import com.brainheap.android.model.ItemView
import com.brainheap.android.network.RetrofitFactory
import com.facebook.FacebookSdk.getApplicationContext
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
    private var titleEditText: EditText? = null
    private var descriptionEditText: EditText? = null
    private var showTranslatedTextCheckBox: CheckBox? = null
    private var translationEditText: EditText? = null

    private val retrofitService = RetrofitFactory.makeRetrofitService()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.words_edit_upload_fragment, container, false)
    }

    private fun initControls() {
        titleEditText = activity?.findViewById(R.id.titleEditText)
        descriptionEditText = activity?.findViewById(R.id.descriptionEditText)
        showTranslatedTextCheckBox = activity?.findViewById(R.id.edit_show_translated_text_checkBox)
        translationEditText = activity?.findViewById(R.id.translatedEditText)
        activity?.let {
            viewModel = ViewModelProviders.of(it).get(WordsEditUploadViewModel::class.java)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initControls()

        viewModel.titleText.observe(this, Observer<String> { titleEditText?.setText(it ?: "") })
        viewModel.descriptionText.observe(this, Observer<String> { descriptionEditText?.setText(it ?: "") })
        viewModel.translationText.observe(this, Observer<String> { translationEditText?.setText(it ?: "") })
        viewModel.showTranslation.observe(this, Observer<Boolean> { showTranslatedTextCheckBox?.isChecked = it })

        viewModel.itemSaved.observe(this, Observer<Boolean> {
            if (it) {
                activity!!.setResult(Activity.RESULT_OK)
                activity!!.finish()
            }
        })

        edit_send_to_server_button.setOnClickListener {
            val userId = viewModel.getUserId()
            val title = titleEditText?.editableText.toString()
            val description = descriptionEditText?.editableText.toString()
            val translation = translationEditText?.editableText.toString()
            if (userId.isNullOrEmpty()) {
                Toast.makeText(getApplicationContext(), "User is not registered", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(
                    getApplicationContext(),
                    "Pick some words for title and description",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            Toast.makeText(getApplicationContext(), "Trying to create item", Toast.LENGTH_SHORT).show()

            CoroutineScope(Dispatchers.IO).launch {
                var toastMessage: String
                try {
                    val createItemRequest = retrofitService
                        .createItemAsync(
                            userId,
                            ItemView(title, description + translation.let { " /// $translation" })
                        )
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
                    Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
        edit_show_translated_text_checkBox.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setShowTranslatedText(isChecked)
        }
    }
}