package com.parinexus.datastore

import androidx.datastore.core.CorruptionException
import com.parinexus.datastore.userPreferences
import com.parinexus.datastore.UserPreferencesSerializer
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.ByteArrayInputStream
import kotlin.test.assertEquals

class UserPreferencesSerializerTest {
    private val userPreferencesSerializer = UserPreferencesSerializer()

    @Test
    fun defaultUserPreferences_isEmpty() {
        assertEquals(
            userPreferences {
            },
            userPreferencesSerializer.defaultValue,
        )
    }

    @Test(expected = CorruptionException::class)
    fun readingInvalidUserPreferences_throwsCorruptionException() = runTest {
        userPreferencesSerializer.readFrom(ByteArrayInputStream(byteArrayOf(0)))
    }
}
