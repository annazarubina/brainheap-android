package com.brainheap.android.ui.wordslist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.brainheap.android.model.Item
import com.brainheap.android.repository.ItemRepository

class WordsListViewModel: ViewModel() {
    private val itemRepositry =  ItemRepository.instance
    var liveDataItemList:LiveData<List<Item>> = itemRepositry.getItems()

    fun refresh() {
        itemRepositry.syncList(true)
    }
}