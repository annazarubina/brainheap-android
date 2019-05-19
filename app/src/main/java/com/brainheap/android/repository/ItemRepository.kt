package com.brainheap.android.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.brainheap.android.CredentialsHolder
import com.brainheap.android.model.Item
import com.brainheap.android.network.RetrofitFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ItemRepository {
    private val retrofitService = RetrofitFactory.makeRetrofitService()
    private val liveItemsList =  MutableLiveData<List<Item>>(emptyList())
    val isRefreshing = MutableLiveData<Boolean>(false)


    fun getItems(): LiveData<List<Item>> {
        syncList(false)
        return liveItemsList
    }

    fun getItem(id: String): Item? {
        syncList(false)
        return liveItemsList.value!!.firstOrNull { id == it.id }
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
                    val itemListResponse = retrofitService.findItemsAsync(CredentialsHolder.userId!!).await()
                    if (itemListResponse.isSuccessful) {
                        liveItemsList.postValue(itemListResponse.body())
                    }
                }  catch (e: HttpException) {
                    //toastMessage = "Exception ${e.message}"

                } catch (e: Throwable) {
                    //toastMessage = "Exception ${e.message}"
                }
                isRefreshing.postValue(false)
            }
        }

    }

    companion object {
        val instance = ItemRepository()

    }

}