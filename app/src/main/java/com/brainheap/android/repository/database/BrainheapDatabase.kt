package com.brainheap.android.repository.database

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [QueueCallItem::class], version = 1)
abstract class BrainheapDatabase : RoomDatabase() {
    abstract fun queueCallDao(): QueueCallDao

    companion object {
        private var db: BrainheapDatabase? = null

        fun instance(app: Application): BrainheapDatabase {
            return db ?: let {
                db = Room
                    .databaseBuilder(app.applicationContext, BrainheapDatabase::class.java, "database.db")
                    .build()
                db!!
            }
        }
    }
}
