package com.parinexus.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.parinexus.database.model.NotoMindEntity
import com.parinexus.model.NoteType
import kotlinx.coroutines.flow.Flow

@Dao
interface NotepadDao {
    @Transaction
    @Query("SELECT * FROM note_table WHERE noteType = :noteType ORDER BY id DESC")
    fun getListOfNotePad(noteType: NoteType): Flow<List<NotoMindEntity>>

    @Transaction
    @Query("SELECT * FROM note_table ORDER BY id DESC")
    fun getListOfNotePad(): Flow<List<NotoMindEntity>>

    @Transaction
    @Query("SELECT * FROM note_table WHERE id = :noteId")
    fun getOneNotePad(noteId: Long): Flow<NotoMindEntity?>
}
