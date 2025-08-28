package com.parinexus.data.repository

import com.parinexus.common.IContentManager
import com.parinexus.database.dao.LabelDao
import com.parinexus.database.dao.NoteCheckDao
import com.parinexus.database.dao.NoteDao
import com.parinexus.database.dao.NoteImageDao
import com.parinexus.database.dao.NoteLabelDao
import com.parinexus.database.dao.NotepadDao
import com.parinexus.database.dao.PathDao
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class NoteRepositoryTest {

    private lateinit var noteCheckDao: NoteCheckDao
    private lateinit var noteDao: NoteDao
    private lateinit var noteImageDao: NoteImageDao
    private lateinit var noteLabelDao: NoteLabelDao
    private lateinit var notepadDao: NotepadDao
    private lateinit var labelDao: LabelDao
    private lateinit var pathDao: PathDao
    private lateinit var contentManager: IContentManager

    private lateinit var repo: NoteRepositoryImpl

    @Before
    fun setUp() {
        noteCheckDao = mockk(relaxUnitFun = true)
        noteDao = mockk(relaxUnitFun = true)
        noteImageDao = mockk(relaxUnitFun = true)
        noteLabelDao = mockk(relaxUnitFun = true)
        notepadDao = mockk(relaxUnitFun = true)
        labelDao = mockk(relaxUnitFun = true)
        pathDao = mockk(relaxUnitFun = true)
        contentManager = mockk(relaxUnitFun = true)

        repo = NoteRepositoryImpl(
            noteCheckDao = noteCheckDao,
            noteDao = noteDao,
            noteImageDao = noteImageDao,
            noteLabelDao = noteLabelDao,
            notePadDao = notepadDao,
            labelDao = labelDao,
            pathDao = pathDao,
            contentManager = contentManager
        )
    }

    @Test
    fun timeToString_formatsAMPM() {
        assertEquals("12 : 00 AM", repo.timeToString(LocalTime(0, 0)))
        assertEquals("12 : 30 PM", repo.timeToString(LocalTime(12, 30)))
        assertEquals(" 1 : 05 PM", repo.timeToString(LocalTime(13, 5)))
        assertEquals("11 : 59 PM", repo.timeToString(LocalTime(23, 59)))
        assertEquals("12 : 00 PM", repo.timeToString(LocalTime(12, 0)))
    }

    @Test
    fun dateToString_withLocalDate() {
        val today = kotlinx.datetime.Clock.System.now()
            .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
        val tomorrow = today.plus(1, kotlinx.datetime.DateTimeUnit.DAY)
        val otherYear = LocalDate(today.year - 1, today.month, minOf(today.dayOfMonth, 28))
        val monthName = otherYear.month.name.lowercase().replaceFirstChar { it.uppercaseChar() }

        assertEquals("Today", repo.dateToString(today))
        assertEquals("Tomorrow", repo.dateToString(tomorrow))
        assertEquals("$monthName ${otherYear.dayOfMonth}, ${otherYear.year}", repo.dateToString(otherYear))
    }

    @Test
    fun dateToString_withEpochMillis() {
        val zone = ZoneId.systemDefault()
        val todayNoon = java.time.LocalDate.now(zone).atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
        val tomorrowNoon = java.time.LocalDate.now(zone).plusDays(1).atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
        val pastNoon = java.time.LocalDate.now(zone).minusYears(1).atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
        val pastYear = java.time.Instant.ofEpochMilli(pastNoon).atZone(zone).year
        val pastMonthShort = java.time.Instant.ofEpochMilli(pastNoon).atZone(zone)
            .month.getDisplayName(TextStyle.FULL, Locale.US).replaceFirstChar { it.uppercaseChar() }.substring(0..2)

        val todayStr = repo.dateToString(todayNoon)
        val tomorrowStr = repo.dateToString(tomorrowNoon)
        val pastStr = repo.dateToString(pastNoon)

        assertTrue(todayStr.startsWith("Today "))
        assertTrue(tomorrowStr.startsWith("Tomorrow "))
        assertTrue(pastStr.startsWith("$pastMonthShort "))
        assertTrue(pastStr.contains(", $pastYear"))
    }

    @Test
    fun delegation_saveImage_getUri_getImagePath() {
        every { contentManager.saveImage("x") } returns 42L
        every { contentManager.pictureUri() } returns "u"
        every { contentManager.getImagePath(7) } returns "p"

        assertEquals(42L, repo.saveImage("x"))
        assertEquals("u", repo.getUri())
        assertEquals("p", repo.getImagePath(7))

        verify(exactly = 1) { contentManager.saveImage("x") }
        verify(exactly = 1) { contentManager.pictureUri() }
        verify(exactly = 1) { contentManager.getImagePath(7) }
    }

    @Test
    fun deleteImageNote_callsDao() = runTest {
        repo.deleteImageNote(9L)
        coVerify(exactly = 1) { noteImageDao.deleteById(9L) }
    }

    @Test
    fun deleteCheckNote_callsDao() = runTest {
        repo.deleteCheckNote(id = 3L, noteId = 11L)
        coVerify(exactly = 1) { noteCheckDao.delete(3L, 11L) }
    }

    @Test
    fun deleteNoteCheckByNoteId_callsDao() = runTest {
        repo.deleteNoteCheckByNoteId(5L)
        coVerify(exactly = 1) { noteCheckDao.deleteByNoteId(5L) }
    }
}