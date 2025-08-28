package com.parinexus.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.parinexus.database.model.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(label: TagEntity): Long

    @Query("SELECT id FROM label_table WHERE name = :name LIMIT 1")
    suspend fun getIdByName(name: String): Long?

    @Upsert
    suspend fun upsert(tagEntity: TagEntity): Long

    @Upsert
    suspend fun upsert(tagEntity: List<TagEntity>): List<Long>

    @Query("DELETE FROM label_table WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM label_table")
    fun getAllLabel(): Flow<List<TagEntity>>

    @Query("SELECT * FROM label_table")
    suspend fun getAllLabelsOneShot(): List<TagEntity>

    @Query("SELECT * FROM label_table")
    fun getAllLabels(): Flow<List<TagEntity>>
}
