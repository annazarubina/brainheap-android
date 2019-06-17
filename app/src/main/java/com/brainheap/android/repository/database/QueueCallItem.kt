package com.brainheap.android.repository.database

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.brainheap.android.model.ItemView


@Entity(tableName = "queue", indices = [Index("user_id")])
data class QueueCallItem(
    @ColumnInfo(name = "user_id")
    var userId: String,
    @ColumnInfo(name = "item_id")
    val itemId: String?,
    @Embedded
    val itemView: ItemView?
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0
}