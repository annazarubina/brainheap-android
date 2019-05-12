package com.brainheap.android.ui.wordslist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.brainheap.android.R
import com.brainheap.android.model.Item

class WordsListAdapter(val onClick: (Item) -> Unit) : RecyclerView.Adapter<WordsListViewHolder>() {
    var items: List<Item> = emptyList()

    fun loadItems(newItems: List<Item>) {
        items = newItems
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordsListViewHolder
            = WordsListViewHolder(
        LayoutInflater.from(parent.context)
        .inflate(R.layout.word_list_item, parent, false))


    override fun onBindViewHolder(holder: WordsListViewHolder, position: Int) {
        holder.item = items[position]
        holder.view.setOnClickListener { onClick(items[position]) }
    }
}