package com.brainheap.android.repository.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface QueueCallDao {
    @Insert
    fun insert(vararg item: QueueCallItem)

    @Query("SELECT * FROM queue LIMIT 1")
    fun loadFirst(): QueueCallItem?

    @Delete
    fun delete(item: QueueCallItem?)
}