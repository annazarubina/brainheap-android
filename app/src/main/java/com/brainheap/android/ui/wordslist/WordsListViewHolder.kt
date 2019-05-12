package com.brainheap.android.ui.wordslist

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.brainheap.android.model.Item
import kotlinx.android.synthetic.main.word_list_item.view.*

class WordsListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    var item: Item? = null
        set(value) {
            field = value
            view.word_title_text_view.text = value?.title
        }
}