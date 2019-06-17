package com.brainheap.android.network.client

import com.brainheap.android.repository.database.QueueCallItem

class QueueCallBuilder(val data: QueueCallItem) {
    private val retrofitService = BrainheapClientFactory.get()

    fun build() = data.itemId
        ?.let {
            retrofitService.updateItem(
                data.userId,
                it,
                data.itemView!!
            )
        } ?: let {
        retrofitService.createItem(
            data.userId,
            data.itemView!!
        )
    }
}