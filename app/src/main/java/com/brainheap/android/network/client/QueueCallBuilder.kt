package com.brainheap.android.network.client

import com.brainheap.android.model.Item
import com.brainheap.android.preferences.CredentialsHolder
import com.brainheap.android.repository.database.QueueCallItem
import retrofit2.Call

class QueueCallBuilder(val data: QueueCallItem) {
    private val retrofitService = BrainheapClientFactory.get()

    fun build(): Call<Item>? =
        getJSessionId()
            ?.let {
                return when (data.action) {
                    QueueCallItem.Action.CREATE -> retrofitService.createItem(
                        data.itemView!!
                    )
                    QueueCallItem.Action.UPDATE -> retrofitService.updateItem(
                        data.itemId!!,
                        data.itemView!!
                    )
                    QueueCallItem.Action.DELETE -> retrofitService.deleteItem(
                        data.itemId!!
                    )
                }
            }

    private fun getJSessionId(): String? = CredentialsHolder.jSessionId.value
}