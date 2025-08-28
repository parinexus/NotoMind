package com.parinexus.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class NotoMindEntity(
    @Embedded
    val noteEntity: NoteEntity,
    @Relation(parentColumn = "id", entityColumn = "noteId")
    val images: List<NoteImageEntity>,
    @Relation(parentColumn = "id", entityColumn = "noteId")
    val checks: List<NoteCheckEntity>,

    @Relation(entity = NoteLabelEntity::class, parentColumn = "id", entityColumn = "note_id")
    val labels: List<FullLabel>,
)
