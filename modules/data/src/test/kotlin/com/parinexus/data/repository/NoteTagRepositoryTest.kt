package com.parinexus.data.repository

import app.cash.turbine.test
import com.parinexus.data.model.toLabel
import com.parinexus.data.model.toLabelEntity
import com.parinexus.data.model.toNoteLabel
import com.parinexus.data.model.toNoteLabelEntity
import com.parinexus.database.dao.LabelDao
import com.parinexus.database.dao.NoteLabelDao
import com.parinexus.database.model.LabelEntity
import com.parinexus.model.Label
import com.parinexus.model.NoteLabel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LabelRepositoryTest {

    private lateinit var labelDao: LabelDao
    private lateinit var noteLabelDao: NoteLabelDao
    private lateinit var repository: NoteTagRepositoryImpl

    @Before
    fun setup() {
        labelDao = mockk(relaxed = true)
        noteLabelDao = mockk(relaxed = true)
        repository = NoteTagRepositoryImpl(labelDao, noteLabelDao)
    }

    @Test
    fun `upsert delegates to LabelDao with mapped entities`() = runTest {
        val labels = listOf(sampleLabel(1), sampleLabel(2))
        coEvery { labelDao.upsert(any<LabelEntity>()) } returns 1L

        repository.upsert(labels)

        coVerify(exactly = 1) {
            labelDao.upsert(labels.map { it.toLabelEntity() })
        }
    }

    @Test
    fun `upsertNoteLabel delegates to NoteLabelDao with mapped entities`() = runTest {
        val pairs = listOf(sampleNoteLabel(noteId = 10, labelId = 1), sampleNoteLabel(11, 2))
        coEvery { noteLabelDao.upsert(any()) } returns Unit

        repository.upsertNoteLabel(pairs)

        coVerify(exactly = 1) {
            noteLabelDao.upsert(pairs.map { it.toNoteLabelEntity() })
        }
    }

    @Test
    fun `getNoteLabel maps entities to domain`() = runTest {
        val id = 42L
        val e1 = sampleNoteLabel(noteId = id, labelId = 7).toNoteLabelEntity()
        val e2 = sampleNoteLabel(noteId = id, labelId = 8).toNoteLabelEntity()

        every { noteLabelDao.getAll(id) } returns flowOf(listOf(e1, e2))

        repository.getNoteLabel(id).test {
            val items = awaitItem()
            assertEquals(listOf(e1.toNoteLabel(), e2.toNoteLabel()), items)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `deleteNoteLabel delegates to NoteLabelDao`() = runTest {
        val noteIds = setOf(1L, 2L, 3L)
        val labelId = 9L
        coEvery { noteLabelDao.delete(any(), any()) } returns Unit

        repository.deleteNoteLabel(noteIds, labelId)

        coVerify(exactly = 1) { noteLabelDao.delete(noteIds, labelId) }
    }

    @Test
    fun `getOneLabelList returns mapped list once`() = runTest {
        val e1 = sampleLabel(1).toLabelEntity()
        val e2 = sampleLabel(2).toLabelEntity()
        coEvery { labelDao.getAllLabelsOneShot() } returns listOf(e1, e2)

        val result = repository.getOneLabelList()

        assertEquals(listOf(e1.toLabel(), e2.toLabel()), result)
    }

    @Test
    fun `getAllLabels maps flow`() = runTest {
        val e1 = sampleLabel(1).toLabelEntity()
        val e2 = sampleLabel(2).toLabelEntity()
        every { labelDao.getAllLabels() } returns flowOf(listOf(e1, e2))

        repository.getAllLabels().test {
            val items = awaitItem()
            assertEquals(listOf(e1.toLabel(), e2.toLabel()), items)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `delete removes label and clears note labels`() = runTest {
        val id = 55L
        coEvery { labelDao.delete(id) } returns Unit
        coEvery { noteLabelDao.clearNoteLabels(id) } returns Unit

        repository.delete(id)

        coVerify(exactly = 1) { labelDao.delete(id) }
        coVerify(exactly = 1) { noteLabelDao.clearNoteLabels(id) }
    }
}

/* ---------- Test data helpers ----------
 * ⚠️ Replace the constructors/fields to match your real models.
 */
private fun sampleLabel(id: Long = 1L): Label =
    Label(
        id = id,
        label = "Label $id" // add/adjust other fields if your Label needs them
    )

private fun sampleNoteLabel(noteId: Long = 1L, labelId: Long = 1L): NoteLabel =
    NoteLabel(
        noteId = noteId,
        labelId = labelId // add/adjust fields if your NoteLabel needs them
    )
