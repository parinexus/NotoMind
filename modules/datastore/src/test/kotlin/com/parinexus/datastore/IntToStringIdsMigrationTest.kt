package com.parinexus.datastore

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IntToStringIdsMigrationTest {

    private fun samplePrefs(): UserPreferences {
        return UserPreferences.getDefaultInstance().copy {
        }
    }

    @Test
    fun shouldMigrate_alwaysTrue() = runTest {
        val prefs = samplePrefs()
        assertTrue(IntToStringIdsMigration.shouldMigrate(prefs))
    }

    @Test
    fun migrate_isNoOp_preservesData() = runTest {
        val before = samplePrefs()
        val after = IntToStringIdsMigration.migrate(before)
        assertEquals(before, after)
    }

    @Test
    fun cleanUp_doesNotThrow() = runTest {
        IntToStringIdsMigration.cleanUp()
    }
}