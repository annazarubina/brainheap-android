package com.brainheap.android.ui.wordslist

import androidx.lifecycle.ViewModel
import com.brainheap.android.repository.ItemRepository
import com.brainheap.android.repository.ItemsListPeriod

class WordsListViewModel : ViewModel() {
    val itemRepositry = ItemRepository.instance

    fun refresh() {
        itemRepositry.syncList(true)
    }

    fun deleteItem(id: String) {
        itemRepositry.deleteItem(id)
    }

    fun setWordsListPeriod(newPeriod: ItemsListPeriod) {
        itemRepositry.setItemsListPeriod(newPeriod)
    }
}