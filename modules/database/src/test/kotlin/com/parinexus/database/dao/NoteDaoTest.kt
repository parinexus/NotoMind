package com.parinexus.database.dao

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.core.app.ApplicationProvider
import com.parinexus.database.NotoMindDatabase
import com.parinexus.database.model.NoteEntity
import com.parinexus.model.NoteType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class NoteDaoTest {

    private lateinit var db: NotoMindDatabase
    private lateinit var dao: NoteDao
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, NotoMindDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.getNoteDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun note(
        id: Long,
        title: String = "t$id",
        detail: String = "c$id",
        editDate: Long = System.currentTimeMillis(),
        isCheck: Boolean = false,
        color: Int = 0,
        background: Int = 0,
        isPin: Boolean = false,
        reminder: Long = 0L,
        interval: Long = 0L,
        noteType: NoteType = NoteType.NOTE
    ): NoteEntity {
        return NoteEntity(
            id = id,
            title = title,
            detail = detail,
            editDate = editDate,
            isCheck = isCheck,
            color = color,
            background = background,
            isPin = isPin,
            reminder = reminder,
            interval = interval,
            noteType = noteType
        )
    }

    private fun countById(id: Long): Int {
        val sql = SimpleSQLiteQuery("SELECT COUNT(*) FROM note_table WHERE id = ?", arrayOf(id))
        db.openHelper.readableDatabase.query(sql).use { c ->
            c.moveToFirst()
            return c.getInt(0)
        }
    }

    @Test
    fun upsert_single_inserts_row() = runTest {
        dao.upsert(note(1L))
        assertEquals(1, countById(1L))
    }

    @Test
    fun upsert_list_inserts_multiple_rows() = runTest {
//        dao.upsert(listOf(note(1L), note(2L), note(3L)))
        assertEquals(1, countById(1L))
        assertEquals(1, countById(2L))
        assertEquals(1, countById(3L))
    }

    @Test
    fun delete_removes_row() = runTest {
        dao.upsert(note(9L))
        assertEquals(1, countById(9L))
        dao.delete(9L)
        assertEquals(0, countById(9L))
    }

    @Test
    fun upsert_same_id_updates_not_duplicates() = runTest {
        dao.upsert(note(5L, title = "A"))
        dao.upsert(note(5L, title = "B"))
        assertEquals(1, countById(5L))
    }
}