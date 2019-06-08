package com.brainheap.android.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.brainheap.android.preferences.CredentialsHolder
import com.brainheap.android.model.Item
import com.brainheap.android.network.RetrofitFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*

enum class ItemsListPeriod(idx: Int){
    TODAY(0),
    THIS_WEEK(1),
    THIS_MONTH(2),
    ALL(3)
}

class ItemRepository {
    private val retrofitService = RetrofitFactory.makeRetrofitService()
    private val liveItemsList =  MutableLiveData<List<Item>>(emptyList())
    private val period = MutableLiveData<ItemsListPeriod>(ItemsListPeriod.TODAY)
    val isRefreshing = MutableLiveData<Boolean>(false)

    fun setItemsListPeriod(newPeriod: ItemsListPeriod) {
        if (newPeriod != period.value) {
            syncList(true)
            period.value = newPeriod
        }
    }

    fun getItems(): LiveData<List<Item>> {
        syncList(false)
        return liveItemsList
    }

    fun getItem(id: String): Item? {
        syncList(false)
        return liveItemsList.value!!.firstOrNull { id == it.id }
    }

    fun getPeriod() : LiveData<ItemsListPeriod> {
        return period
    }

    fun deleteItem(itemId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            isRefreshing.postValue(true)
            try {
                val itemDeleteResponse = retrofitService.deleteItemAsync(CredentialsHolder.userId!!, itemId).await()
                if (itemDeleteResponse.isSuccessful) {
                    val copyList = liveItemsList.value!!.filter{ itemId != it.id }
                    liveItemsList.postValue(copyList)
                }
            } catch (e: HttpException) {
                //toastMessage = "Exception ${e.message}"

            } catch (e: Throwable) {
                //toastMessage = "Exception ${e.message}"
            }
            isRefreshing.postValue(false)
        }
    }

    fun syncList(force: Boolean) {
        if (liveItemsList.value.isNullOrEmpty() or force) {
            CoroutineScope(Dispatchers.IO).launch {
                isRefreshing.postValue(true)
                try {
                    val query = queryFromPeriod(period.value!!)
                    val itemListResponse = retrofitService.findItemsAsync(CredentialsHolder.userId!!,query).await()
                    if (itemListResponse.isSuccessful) {
                        liveItemsList.postValue(itemListResponse.body())
                    }
                }  catch (e: HttpException) {
                    //toastMessage = "Exception ${e.message}"

                } catch (e: Throwable) {
                    "Exception ${e.message}"
                }
                isRefreshing.postValue(false)
            }
        }

    }

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

    companion object {
        val instance = ItemRepository()

    }

}