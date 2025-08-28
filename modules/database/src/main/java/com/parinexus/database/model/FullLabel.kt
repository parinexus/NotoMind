package com.parinexus.database.model

import androidx.room.Embedded
import androidx.room.Relation

data class FullLabel(
    @Embedded
    val noteLabel: NoteLabelEntity,
    @Relation(entity = LabelEntity::class, entityColumn = "id", parentColumn = "label_id")
    val label: LabelEntity,
)
