package com.parinexus.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.parinexus.database.dao.TagDao
import com.parinexus.database.dao.NoteCheckDao
import com.parinexus.database.dao.NoteDao
import com.parinexus.database.dao.NoteImageDao
import com.parinexus.database.dao.NoteLabelDao
import com.parinexus.database.dao.NotepadDao
import com.parinexus.database.dao.PathDao
import com.parinexus.database.model.DrawPathEntity
import com.parinexus.database.model.TagEntity
import com.parinexus.database.model.NoteCheckEntity
import com.parinexus.database.model.NoteEntity
import com.parinexus.database.model.NoteImageEntity
import com.parinexus.database.model.NoteLabelEntity

@Database(
    entities = [
        NoteEntity::class,
        NoteImageEntity::class,
        NoteCheckEntity::class,
        NoteLabelEntity::class,
        TagEntity::class,
        DrawPathEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class NotoMindDatabase : RoomDatabase() {

    abstract fun getLabelDao(): TagDao

    abstract fun getNoteCheckDao(): NoteCheckDao

    abstract fun getNoteDao(): NoteDao

    abstract fun getNoteImageDao(): NoteImageDao

    abstract fun getNoteLabelDao(): NoteLabelDao

    abstract fun getNotePadDao(): NotepadDao

    abstract fun getPath(): PathDao
}
