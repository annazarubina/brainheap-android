package com.brainheap.android.ui.wordslist

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.brainheap.android.model.Item
import com.brainheap.android.repository.ItemRepository
import com.brainheap.android.repository.ItemsListPeriod

class WordsListViewModel: ViewModel() {
    private val itemRepositry =  ItemRepository.instance
    var liveDataItemList:LiveData<List<Item>> = itemRepositry.getItems()
    val isRefreshing: LiveData<Boolean> = itemRepositry.isRefreshing
    val period: LiveData<ItemsListPeriod> = itemRepositry.getPeriod()

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