package com.brainheap.android.network.client

import androidx.lifecycle.MutableLiveData
import com.brainheap.android.BrainheapApp
import com.brainheap.android.model.ItemView
import com.brainheap.android.repository.database.BrainheapDatabase
import com.brainheap.android.repository.database.QueueCallDao
import com.brainheap.android.repository.database.QueueCallItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext

class QueueCallExecutor {
    private var job: Job? = null
    private val dao: QueueCallDao = BrainheapDatabase.instance(BrainheapApp.application()).queueCallDao()
    val callSucceed = MutableLiveData<QueueCallItem>()

   @Synchronized
    fun add(action: QueueCallItem.Action, userId: String, itemId: String?, itemView: ItemView?) {
        CoroutineScope(newSingleThreadContext(this.javaClass.name + ":add")).launch {
            dao.insert(QueueCallItem(action, userId, itemId, itemView))
        }
    }

    fun start() {
        job?.let {
            return
        } ?: let {
            val scope = CoroutineScope(newSingleThreadContext(this.javaClass.name + ":start"))
            job = scope.launch {
                while (true) {
                    val item = dao.loadFirst()
                    item
                        ?.let { execute(it) }
                        ?.takeIf { it }
                        ?.let { dao.delete(item) }
                }
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private fun execute(data: QueueCallItem): Boolean {
        return try {
            if (QueueCallBuilder(data).build().execute().isSuccessful) {
                callSucceed.postValue(data)
                return true
            }
            return false
        } catch (e: Throwable) {
            false
        }
    }
}