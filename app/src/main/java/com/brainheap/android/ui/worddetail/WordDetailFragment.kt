package com.brainheap.android.ui.worddetail

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.Html.FROM_HTML_MODE_COMPACT
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.brainheap.android.R
import com.brainheap.android.model.Item
import com.brainheap.android.repository.ItemRepository
import com.brainheap.android.ui.wordseditupload.WordsEditUploadActivity
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_word_detail.*

class WordDetailFragment : Fragment() {
    private val EDIT_WORDS_REQUEST = 1  // The request code

    private var itemId: String? = null
    private var item: Item? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_word_detail, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        word_detail_text_view.keyListener = null

        itemId = WordDetailFragmentArgs.fromBundle(arguments!!).ItemId

        word_detail_edit_item_button.setOnClickListener {
            startEditActivity()
        }

        ItemRepository.instance.isRefreshing.observe(this, Observer<Boolean> {
            word_detail_text_view.isEnabled = !it
            word_detail_edit_item_button.isEnabled = !it
            when(it) {
                true -> word_detail_loading_spinner.visibility = View.VISIBLE
                false -> word_detail_loading_spinner.visibility = View.GONE
            }
        })

        ItemRepository.instance.liveItemsList.observe(this, Observer<List<Item>> {
            loadItem()
            word_detail_text_view.setText(getItemHtmlText())
        })
    }

    private fun loadItem() {
        itemId?.let {
            item = ItemRepository.instance.getItem(it)
        }
    }

    private fun startEditActivity() {
        startEditActivityForDescription(HtmlTextBuilder(item).splitDescription(item?.description))
    }

    private fun startEditActivityForDescription(splitDescription: HtmlTextBuilder.SplitDescription) {
        val intent = Intent(this.context, WordsEditUploadActivity::class.java)
        intent.putExtra("itemId", itemId)
        intent.putExtra("title", item?.title)
        intent.putExtra("description", splitDescription.description)
        intent.putExtra("translation", splitDescription.translation)
        startActivityForResult(intent, EDIT_WORDS_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == EDIT_WORDS_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                ItemRepository.instance.syncList(true)
            }
        }
    }

    private fun getItemHtmlText(): CharSequence {
        return Html.fromHtml(HtmlTextBuilder(item).process(), FROM_HTML_MODE_COMPACT)
    }
}
