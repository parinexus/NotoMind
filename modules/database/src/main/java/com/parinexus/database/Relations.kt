package com.parinexus.database

import androidx.room.*
import com.parinexus.database.model.TagEntity
import com.parinexus.database.model.NoteCheckEntity
import com.parinexus.database.model.NoteEntity
import com.parinexus.database.model.NoteImageEntity
import com.parinexus.database.model.NoteLabelEntity

data class NoteWithLabels(
    @Embedded val note: NoteEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteLabelEntity::class,
            parentColumn = "note_id",
            entityColumn = "label_id"
        )
    )
    val labels: List<TagEntity>,

    @Relation(parentColumn = "id", entityColumn = "noteId")
    val images: List<NoteImageEntity>,

    @Relation(parentColumn = "id", entityColumn = "noteId")
    val checks: List<NoteCheckEntity>,
)

data class LabelWithNotes(
    @Embedded val label: TagEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = NoteLabelEntity::class,
            parentColumn = "label_id",
            entityColumn = "note_id"
        )
    )
    val notes: List<NoteEntity>
)
