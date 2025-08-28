package com.parinexus.domain.repository

import com.parinexus.model.NotoMind
import com.parinexus.model.NoteType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

interface NoteRepository {

    suspend fun upsert(notoMind: NotoMind): Long
    suspend fun upsert(notoMinds: List<NotoMind>)

    suspend fun deleteCheckNote(id: Long, noteId: Long)

    suspend fun deleteNoteCheckByNoteId(noteId: Long)
    fun getNotePads(noteType: NoteType): Flow<List<NotoMind>>
    fun getNotePads(): Flow<List<NotoMind>>

    fun getOneNotePad(id: Long): Flow<NotoMind?>

    suspend fun deleteNotePad(notoMinds: List<NotoMind>)

    suspend fun delete(notoMinds: List<NotoMind>)

    fun timeToString(time: LocalTime): String
    fun dateToString(date: LocalDate): String
    fun dateToString(long: Long): String

    fun saveImage(uri: String): Long
    fun getUri(): String
    fun getImagePath(id: Long): String

    suspend fun deleteImageNote(id: Long)
}
