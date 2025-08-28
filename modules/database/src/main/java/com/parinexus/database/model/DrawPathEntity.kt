package com.parinexus.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "path_table",
    foreignKeys = [
        ForeignKey(
            entity = NoteImageEntity::class,
            parentColumns = ["id"],
            childColumns = ["image_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
)
data class DrawPathEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    @ColumnInfo(name = "image_id")
    val imageId: Long,

    @ColumnInfo(name = "color")
    val color: Int,

    @ColumnInfo(name = "width")
    val width: Int,

    @ColumnInfo(name = "stroke_join")
    val join: Int,

    @ColumnInfo(name = "alpha")
    val alpha: Float,

    @ColumnInfo(name = "stroke_cap")
    val cap: Int,

    @ColumnInfo(name = "paths")
    val paths: String,

    @ColumnInfo(name = "order_index")
    val orderIndex: Int = 0
)