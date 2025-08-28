package com.parinexus.database.di

import android.content.Context
import androidx.room.Room
import com.parinexus.database.NotoMindDatabase
import com.parinexus.database.dao.TagDao
import com.parinexus.database.dao.NoteCheckDao
import com.parinexus.database.dao.NoteDao
import com.parinexus.database.dao.NoteImageDao
import com.parinexus.database.dao.NoteLabelDao
import com.parinexus.database.dao.NotepadDao
import com.parinexus.database.dao.PathDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun databaseProvider(
        @ApplicationContext context: Context,
    ): NotoMindDatabase {
        return Room.databaseBuilder(context, NotoMindDatabase::class.java, "notoMindDb.db")
            .build()
    }

    @Provides
    @Singleton
    fun labelDaoProvider(notoMindDatabase: NotoMindDatabase): TagDao {
        return notoMindDatabase.getLabelDao()
    }

    @Provides
    @Singleton
    fun noteCheckDaoProvider(notoMindDatabase: NotoMindDatabase): NoteCheckDao {
        return notoMindDatabase.getNoteCheckDao()
    }

    @Provides
    @Singleton
    fun noteDaoProvider(notoMindDatabase: NotoMindDatabase): NoteDao {
        return notoMindDatabase.getNoteDao()
    }

    @Provides
    @Singleton
    fun noteImageDaoProvider(notoMindDatabase: NotoMindDatabase): NoteImageDao {
        return notoMindDatabase.getNoteImageDao()
    }

    @Provides
    @Singleton
    fun noteLabelDaoProvider(notoMindDatabase: NotoMindDatabase): NoteLabelDao {
        return notoMindDatabase.getNoteLabelDao()
    }

    @Provides
    @Singleton
    fun notePadDaoProvider(notoMindDatabase: NotoMindDatabase): NotepadDao {
        return notoMindDatabase.getNotePadDao()
    }

    @Provides
    @Singleton
    fun pathDaoProvider(notoMindDatabase: NotoMindDatabase): PathDao {
        return notoMindDatabase.getPath()
    }
}
