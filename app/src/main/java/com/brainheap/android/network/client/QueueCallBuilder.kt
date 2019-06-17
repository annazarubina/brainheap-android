package com.brainheap.android.network.client

import com.brainheap.android.model.Item
import com.brainheap.android.repository.database.QueueCallItem
import retrofit2.Call

class QueueCallBuilder(val data: QueueCallItem) {
    private val retrofitService = BrainheapClientFactory.get()

    fun build() : Call<Item> {
        return when(data.action) {
            QueueCallItem.Action.CREATE -> retrofitService.createItem(
                data.userId,
                data.itemView!!
            )
            QueueCallItem.Action.UPDATE -> retrofitService.updateItem(
                data.userId,
                data.itemId!!,
                data.itemView!!
            )
            QueueCallItem.Action.DELETE -> retrofitService.deleteItem(
                data.userId,
                data.itemId!!
            )
        }
    }
}