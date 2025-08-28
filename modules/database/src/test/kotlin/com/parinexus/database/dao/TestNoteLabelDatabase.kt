package com.parinexus.database.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import com.parinexus.database.model.NoteLabelEntity

@Database(
    entities = [NoteLabelEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TestNoteLabelDatabase : RoomDatabase() {
    abstract fun noteLabelDao(): NoteLabelDao
    abstract fun labelDao(): TagDao
}
