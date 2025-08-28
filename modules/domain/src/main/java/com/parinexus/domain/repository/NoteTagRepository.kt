package com.parinexus.domain.repository

import com.parinexus.model.Label
import com.parinexus.model.NoteLabel
import kotlinx.coroutines.flow.Flow

interface NoteTagRepository {
    suspend fun upsert(labels: List<Label>): List<Long>
    suspend fun upsertNoteLabel(notelabels: List<NoteLabel>)
    fun getNoteLabel(id: Long): Flow<List<NoteLabel>>
    suspend fun deleteNoteLabel(noteIds: Set<Long>, labelId: Long)

    suspend fun getOneLabelList(): List<Label>
    fun getAllLabels(): Flow<List<Label>>

    suspend fun delete(id: Long)
}
