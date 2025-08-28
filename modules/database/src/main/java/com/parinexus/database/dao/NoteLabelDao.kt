package com.parinexus.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.parinexus.database.model.NoteLabelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteLabelDao {
    @Upsert
    suspend fun upsert(crossRefs: List<NoteLabelEntity>)

    @Query("DELETE FROM note_label_table WHERE note_id = :noteId")
    suspend fun clearNoteLabels(noteId: Long)

    @Query("SELECT * FROM note_label_table WHERE note_id = :id")
    fun getAll(id: Long): Flow<List<NoteLabelEntity>>

    @Query("DELETE FROM note_label_table WHERE note_id IN (:ids) AND label_id = :labelId")
    suspend fun delete(ids: Set<Long>, labelId: Long)
}