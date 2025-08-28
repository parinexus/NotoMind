package com.parinexus.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.parinexus.database.NotoMindDatabase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class PathDaoTest {

    private lateinit var db: NotoMindDatabase
    private lateinit var dao: PathDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, NotoMindDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.getPath()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun getPaths_initially_empty() = runTest {
        dao.getPaths(100L).test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun delete_on_empty_keeps_flow_empty() = runTest {
        dao.delete(100L)
        dao.getPaths(100L).test {
            assertTrue(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }
}