package com.parinexus.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.parinexus.database.model.NoteLabelEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class NoteLabelDaoTest {

    private lateinit var db: TestNoteLabelDatabase
    private lateinit var dao: NoteLabelDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TestNoteLabelDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        dao = db.noteLabelDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsert_and_getAll_emitsInsertedCrossRefs() = runTest {
        val noteId = 1L
        val rows = listOf(
            NoteLabelEntity(noteId = noteId, labelId = 10L),
            NoteLabelEntity(noteId = noteId, labelId = 11L),
        )

        dao.upsert(rows)

        dao.getAll(noteId).test {
            val emitted = awaitItem()
            assertEquals(2, emitted.size)
            val pairSet = emitted.map { it.noteId to it.labelId }.toSet()
            assertTrue((noteId to 10L) in pairSet)
            assertTrue((noteId to 11L) in pairSet)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearNoteLabels_removesOnlyForSpecifiedNote() = runTest {
        val rows = listOf(
            NoteLabelEntity(noteId = 1L, labelId = 10L),
            NoteLabelEntity(noteId = 1L, labelId = 11L),
            NoteLabelEntity(noteId = 2L, labelId = 10L),
        )
        dao.upsert(rows)

        dao.clearNoteLabels(1L)

        dao.getAll(1L).test {
            val emitted = awaitItem()
            assertTrue(emitted.isEmpty(), "Expected note 1 to have no labels after clear.")
            cancelAndIgnoreRemainingEvents()
        }
        dao.getAll(2L).test {
            val emitted = awaitItem()
            assertEquals(1, emitted.size)
            assertEquals(2L, emitted.first().noteId)
            assertEquals(10L, emitted.first().labelId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun delete_byNoteIdsAndLabelId_removesOnlyMatchingRows() = runTest {
        val rows = listOf(
            NoteLabelEntity(noteId = 1L, labelId = 10L),
            NoteLabelEntity(noteId = 1L, labelId = 11L),
            NoteLabelEntity(noteId = 2L, labelId = 10L),
            NoteLabelEntity(noteId = 3L, labelId = 10L),
        )
        dao.upsert(rows)

        dao.delete(ids = setOf(1L, 3L), labelId = 10L)

        dao.getAll(1L).test {
            val emitted = awaitItem()
            assertEquals(1, emitted.size)
            assertEquals(11L, emitted.first().labelId)
            cancelAndIgnoreRemainingEvents()
        }
        dao.getAll(2L).test {
            val emitted = awaitItem()
            assertEquals(1, emitted.size)
            assertEquals(10L, emitted.first().labelId)
            cancelAndIgnoreRemainingEvents()
        }
        dao.getAll(3L).test {
            val emitted = awaitItem()
            assertTrue(emitted.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
}