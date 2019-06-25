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
import com.brainheap.android.repository.ItemRepository
import com.brainheap.android.ui.worddetail.HtmlTextBuilder
import kotlinx.android.synthetic.main.words_edit_upload_fragment.*

class WordsEditUploadFragment : Fragment() {
    companion object {
        fun newInstance() = WordsEditUploadFragment()
    }

    private lateinit var viewModel: WordsEditUploadViewModel
    val itemRepositry = ItemRepository.instance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.words_edit_upload_fragment, container, false)

    private fun initControls() {
        titleEditText?.setText(viewModel.title ?: "")
        descriptionEditText?.setText(viewModel.description ?: "")
        translatedEditText?.setText(viewModel.translation.value ?: "")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let {
            viewModel = ViewModelProviders.of(it).get(WordsEditUploadViewModel::class.java)
        }
        initControls()

        viewModel.showTranslation.observe(this, Observer<Boolean> {
            edit_show_translated_text_checkBox?.isChecked = it
            viewModel.loadTranslation(descriptionEditText.editableText.toString())
            updateSyncTranslationButtonState()
        })

        viewModel.translation.observe(this, Observer {
            translatedEditText?.setText(it ?: "")
        })

        viewModel.itemSaved.observe(this, Observer<Boolean> { saved ->
            if (saved) {
                viewModel.save()
                activity!!.setResult(Activity.RESULT_OK)
                activity!!.finish()
            }
        })

        edit_send_to_server_button.setOnClickListener {
            val itemId = viewModel.itemId?.takeIf { it.isNotEmpty() }
            val userId = viewModel.userId
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
            itemRepositry.addItem(
                itemId,
                ItemView(title, HtmlTextBuilder.joinDescription(description, translation) ?: "")
            )
            viewModel.itemSaved.postValue(true)
        }

        descriptionEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                updateSyncTranslationButtonState()
            }
        }

        edit_show_translated_text_checkBox.setOnCheckedChangeListener { _, checked ->
            when (checked) {
                true -> {
                    translatedEditText.visibility = View.VISIBLE
                }
                false -> translatedEditText.visibility = View.GONE
            }
            viewModel.showTranslation.postValue(checked)
        }

        words_edit_upload_sync_translation.setOnClickListener {
            viewModel.loadTranslation(descriptionEditText?.editableText.toString())
        }
    }

    private fun updateSyncTranslationButtonState() {
        when (isSyncTranslationAvalable()) {
            true -> words_edit_upload_sync_translation.visibility = View.VISIBLE
            false -> words_edit_upload_sync_translation.visibility = View.GONE
        }
    }

    private fun isSyncTranslationAvalable(): Boolean {
        return edit_show_translated_text_checkBox.isChecked && viewModel.cashedDescription != descriptionEditText?.editableText.toString()
    }
}