package com.parinexus.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.parinexus.database.model.TagEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LabelDaoTest {

    private lateinit var db: TestNoteLabelDatabase
    private lateinit var dao: TagDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TestNoteLabelDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        dao = db.labelDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insert_and_getIdByName() = runTest {
        val id = dao.insert(TagEntity(name = "alpha"))
        val fetched = dao.getIdByName("alpha")
        assertEquals(id, fetched)
    }

    @Test
    fun upsert_single_insert_then_update() = runTest {
        val insertedId = dao.upsert(TagEntity(name = "beta"))
        var all = dao.getAllLabelsOneShot()
        assertEquals(1, all.size)
        assertEquals("beta", all.single().name)

        dao.upsert(TagEntity(id = insertedId, name = "beta2"))
        all = dao.getAllLabelsOneShot()
        assertEquals(1, all.size)
        assertEquals(insertedId, all.single().id)
        assertEquals("beta2", all.single().name)
    }

    @Test
    fun upsert_list_inserts_multiple() = runTest {
        val ids = dao.upsert(listOf(TagEntity(name = "c1"), TagEntity(name = "c2")))
        assertEquals(2, ids.size)
        val all = dao.getAllLabelsOneShot()
        assertEquals(2, all.size)
        assertTrue(all.any { it.name == "c1" })
        assertTrue(all.any { it.name == "c2" })
    }

    @Test
    fun delete_by_id() = runTest {
        val id = dao.insert(TagEntity(name = "toDelete"))
        var all = dao.getAllLabelsOneShot()
        assertEquals(1, all.size)
        dao.delete(id)
        all = dao.getAllLabelsOneShot()
        assertTrue(all.isEmpty())
    }

    @Test
    fun flows_emit_current_values() = runTest {
        val initialA = dao.getAllLabel().first()
        val initialB = dao.getAllLabels().first()
        assertTrue(initialA.isEmpty())
        assertTrue(initialB.isEmpty())

        dao.insert(TagEntity(name = "flowLabel"))

        val afterA = dao.getAllLabel().first()
        val afterB = dao.getAllLabels().first()
        assertEquals(1, afterA.size)
        assertEquals(1, afterB.size)
        assertEquals("flowLabel", afterA.single().name)
        assertEquals("flowLabel", afterB.single().name)
    }
}