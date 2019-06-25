package com.brainheap.android.repository

import android.annotation.SuppressLint
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.brainheap.android.model.Item
import com.brainheap.android.model.ItemView
import com.brainheap.android.network.client.BrainheapClientFactory
import com.brainheap.android.network.client.QueueCallExecutor
import com.brainheap.android.preferences.AppPreferences
import com.brainheap.android.preferences.Constants.PERIOD_PROP
import com.brainheap.android.preferences.CredentialsHolder
import com.brainheap.android.repository.database.QueueCallItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

enum class ItemsListPeriod(val idx: Int) {
    TODAY(0),
    THIS_WEEK(1),
    THIS_MONTH(2),
    ALL(3);

    companion object {
        fun get(int: Int): ItemsListPeriod? = values().find { it.idx == int }
    }
}

class ItemRepository : LifecycleOwner {
    private val retrofitService = BrainheapClientFactory.get()
    private val lifecycleRegistry = LifecycleRegistry(this)

    val liveItemsList = MutableLiveData<List<Item>>(emptyList())
    val period = MutableLiveData<ItemsListPeriod>(ItemsListPeriod.get(AppPreferences.get().getInt(PERIOD_PROP, 0)))
    val isRefreshing = MutableLiveData<Boolean>(false)
    private val queueCallExecutor = QueueCallExecutor()

    override fun getLifecycle(): Lifecycle = lifecycleRegistry

    init {
        queueCallExecutor.start()
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        queueCallExecutor.callSucceed.observe(this, Observer {
            syncList(true)
        })
    }

    fun setItemsListPeriod(newPeriod: ItemsListPeriod) {
        if (newPeriod != period.value) {
            syncList(true)
            period.value = newPeriod
        }
    }

    fun getItem(id: String): Item? {
        syncList(false)
        return liveItemsList.value!!.firstOrNull { id == it.id }
    }

    fun deleteItem(itemId: String) {
        val item = QueueCallItem(
            QueueCallItem.Action.DELETE,
            CredentialsHolder.userId.value!!, itemId, null
        )

        queueCallExecutor.add(item)
        applyQueueActionToList(item)
    }

    fun addItem(itemId: String?, itemView: ItemView) {
        val item = QueueCallItem(itemId?.let { QueueCallItem.Action.UPDATE } ?: let { QueueCallItem.Action.CREATE },
            CredentialsHolder.userId.value!!,
            itemId,
            itemView
        )

        queueCallExecutor.add(item)
        applyQueueActionToList(item)
    }

    fun syncList(force: Boolean) {
        if (liveItemsList.value.isNullOrEmpty() or force) {
            CredentialsHolder.userId.value?.takeIf { it.isNotEmpty() }?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    isRefreshing.postValue(true)
                    try {
                        val query = queryFromPeriod(period.value!!)
                        val itemListResponse = retrofitService.findItemsAsync(it, query).await()
                        if (itemListResponse.isSuccessful) {
                            liveItemsList.postValue(itemListResponse.body())
                        }
                    } catch (e: HttpException) {
                        //toastMessage = "Exception ${e.message}"

                    } catch (e: Throwable) {
                        "Exception ${e.message}"
                    }
                    isRefreshing.postValue(false)
                }
            } ?: let {
                liveItemsList.postValue(Collections.emptyList())
            }
        }
    }


    @SuppressLint("SimpleDateFormat")
    private fun queryFromPeriod(queryPeriod: ItemsListPeriod): String? {
        val calendar = Calendar.getInstance()
        calendar.clear(Calendar.MINUTE)
        calendar.clear(Calendar.SECOND)
        calendar.clear(Calendar.MILLISECOND) //TODO TZ handling
        val format = SimpleDateFormat("E, d MMM yyyy HH:mm:ss")
        return when (queryPeriod) {
            ItemsListPeriod.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                "created >= \"${format.format(calendar.time)}\""
            }
            ItemsListPeriod.THIS_WEEK -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                "created >= \"${format.format(calendar.time)}\""
            }
            ItemsListPeriod.THIS_MONTH -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                "created >= \"${format.format(calendar.time)}\""
            }
            else -> null
        }
    }

    private fun applyQueueActionToList(list: List<Item>?, queueItem: QueueCallItem): List<Item>? {
        return when (queueItem.action) {
            QueueCallItem.Action.DELETE -> list?.filter { it.id != queueItem.itemId }
            QueueCallItem.Action.CREATE -> {
                queueItem.itemView?.let {
                    val array = list?.let { ArrayList(it) } ?: ArrayList<Item>()
                    array.add(
                        0,
                        Item(
                            "",
                            queueItem.itemView.description,
                            TEMP_ID_PREFIX + generateUUID(),
                            "",
                            queueItem.itemView.title,
                            queueItem.userId
                        )
                    )
                    return array
                } ?: let { return null }
            }
            QueueCallItem.Action.UPDATE -> {
                queueItem.itemView?.let {
                    return list?.map {
                        it.takeIf { it.id != queueItem.itemId }
                            ?: Item(
                                it.created,
                                queueItem.itemView.description,
                                it.id,
                                it.modified,
                                queueItem.itemView.title,
                                it.userId
                            )
                    }
                } ?: let { return null }
            }
        }
    }

    private fun applyQueueActionToList(queueItem: QueueCallItem) {
        applyQueueActionToList(liveItemsList.value, queueItem)?.let { liveItemsList.postValue(it) }
    }

    companion object {
        @JvmStatic
        val TEMP_ID_PREFIX = "temp:"

        @JvmStatic
        private var singleton: ItemRepository? = null

        @JvmStatic
        fun instance(): ItemRepository = singleton?.let { it } ?: let {
            singleton = ItemRepository()
            singleton!!
        }

        @JvmStatic
        fun generateUUID() = UUID.randomUUID().toString()
    }
}