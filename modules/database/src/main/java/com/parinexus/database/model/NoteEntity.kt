package com.parinexus.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.parinexus.model.NoteType


@Entity(tableName = "note_table")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0L,
    val title: String,
    val detail: String,
    val editDate: Long,
    val isCheck: Boolean,
    val color: Int,
    val background: Int,
    val isPin: Boolean,
    val reminder: Long,
    val interval: Long,
    val noteType: NoteType,
)