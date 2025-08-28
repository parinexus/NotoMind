package com.parinexus.labelscreen

import androidx.lifecycle.SavedStateHandle
import com.parinexus.domain.repository.NoteTagRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LabelViewModelTest {

    @get:Rule
    val mainRule = object : TestWatcher() {
        private val dispatcher = StandardTestDispatcher()
        override fun starting(description: org.junit.runner.Description) {
            Dispatchers.setMain(dispatcher)
        }
        override fun finished(description: org.junit.runner.Description) {
            Dispatchers.resetMain()
        }
    }

    private lateinit var repo: NoteTagRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var vm: LabelViewModel

    @Before
    fun setup() {
        savedStateHandle = SavedStateHandle(mapOf("isEditMode" to true))

        repo = mockk()

        coEvery { repo.getOneLabelList() } returns emptyList() // List<Label>

        coEvery { repo.upsert(any()) } returns emptyList<Long>()

        coEvery { repo.delete(any()) } returns Unit
    }

    private fun createVm(): LabelViewModel =
        LabelViewModel(
            savedStateHandle = savedStateHandle,
            labelRepository = repo
        )

    @Test
    fun init_loads_labels_and_applies_isEditMode() = runTest {
        vm = createVm()
        advanceUntilIdle()

        val s = vm.labelScreenUiState
        assertEquals(0, s.labels.size)
        assertEquals(true, s.isEditMode)
    }

    @Test
    fun add_unique_label_updates_state_and_calls_upsert() = runTest {
        vm = createVm()
        advanceUntilIdle()

        vm.onAddLabelChange("Work")
        vm.onAddLabelDone()
        advanceUntilIdle()

        val s = vm.labelScreenUiState
        assertEquals(1, s.labels.size)
        assertEquals("Work", s.labels.first().label)
        assertEquals("", s.editText)
        assertEquals(false, s.errorOccur)

        coVerify(atLeast = 1) { repo.upsert(any()) }
    }

    @Test
    fun add_duplicate_label_sets_error_and_keeps_list_unchanged() = runTest {
        vm = createVm()
        advanceUntilIdle()

        vm.onAddLabelChange("Home")
        vm.onAddLabelDone()
        advanceUntilIdle()
        val sizeAfterFirst = vm.labelScreenUiState.labels.size
        assertEquals(1, sizeAfterFirst)

        vm.onAddLabelChange("Home")
        vm.onAddLabelDone()
        advanceUntilIdle()

        val s = vm.labelScreenUiState
        assertEquals(sizeAfterFirst, s.labels.size)
        assertEquals(true, s.errorOccur)

        coVerify(atLeast = 1) { repo.upsert(any()) }
    }

    @Test
    fun delete_label_updates_state_and_calls_delete_repo() = runTest {
        vm = createVm()
        advanceUntilIdle()

        vm.onAddLabelChange("A")
        vm.onAddLabelDone()
        vm.onAddLabelChange("B")
        vm.onAddLabelDone()
        advanceUntilIdle()

        val before = vm.labelScreenUiState.labels.toImmutableList()
        assertEquals(listOf(1L, 2L), before.map { it.id })

        vm.onDelete(1L)
        advanceUntilIdle()

        val after = vm.labelScreenUiState.labels.toImmutableList()
        assertEquals(listOf(2L), after.map { it.id })

        coVerify(exactly = 1) { repo.delete(1L) }
        coVerify(atLeast = 1) { repo.upsert(any()) }
    }

    @Test
    fun clear_add_text_resets_editText() = runTest {
        vm = createVm()
        advanceUntilIdle()

        vm.onAddLabelChange("Temp")
        assertEquals("Temp", vm.labelScreenUiState.editText)

        vm.onAddDeleteValue()
        assertEquals("", vm.labelScreenUiState.editText)
    }
}