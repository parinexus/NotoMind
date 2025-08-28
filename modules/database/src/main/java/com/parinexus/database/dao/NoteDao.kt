package com.parinexus.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.parinexus.database.NoteWithLabels
import com.parinexus.database.model.NoteEntity

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity): Long

    @Upsert
    suspend fun upsert(note: NoteEntity): Long

    @Transaction
    @Query("SELECT * FROM note_table WHERE id = :id")
    suspend fun getNoteWithLabels(id: Long): NoteWithLabels?

    @Query("SELECT EXISTS(SELECT 1 FROM note_table WHERE id = :id)")
    suspend fun noteExists(id: Long): Boolean

    @Query("DELETE FROM note_table WHERE id = :noteId")
    suspend fun delete(noteId: Long)
}