package com.parinexus.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.parinexus.domain.usecases.*
import com.parinexus.model.NotoMind
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.rules.TestWatcher
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DetailViewModelTest {

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

    private val noteId = 42L
    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var observeNote: ObserveNoteUseCase
    private lateinit var upsertNote: UpsertNoteUseCase
    private lateinit var copyNoteUseCase: CopyNoteUseCase
    private lateinit var archiveNoteUseCase: ArchiveNoteUseCase
    private lateinit var setAlarmUseCase: SetAlarmUseCase
    private lateinit var deleteAlarmUseCase: DeleteAlarmUseCase
    private lateinit var saveImageUseCase: SaveImageUseCase
    private lateinit var getPhotoUriUseCase: GetPhotoUriUseCase
    private lateinit var reminderCoordinator: ReminderCoordinator

    private lateinit var vm: DetailViewModel

    @Before
    fun setup() {
        savedStateHandle = SavedStateHandle(mapOf("id" to noteId))

        observeNote = mockk()
        upsertNote = mockk()
        copyNoteUseCase = mockk(relaxed = true)
        archiveNoteUseCase = mockk(relaxed = true)
        setAlarmUseCase = mockk(relaxed = true)
        deleteAlarmUseCase = mockk(relaxed = true)
        saveImageUseCase = mockk(relaxed = true)
        getPhotoUriUseCase = mockk(relaxed = true)

        reminderCoordinator = mockk(relaxed = true)
        val fakeUi = mockk<ReminderCoordinator.ReminderUi>(relaxed = true)
        every { reminderCoordinator.init(any()) } returns fakeUi
        every { reminderCoordinator.onSetDateIndex(any(), any()) } returns fakeUi
        every { reminderCoordinator.onSetTimeIndex(any(), any()) } returns fakeUi
        every { reminderCoordinator.onSetIntervalIndex(any(), any()) } returns fakeUi
        every { reminderCoordinator.onConfirmDate(any(), any()) } returns fakeUi
        every { reminderCoordinator.onConfirmTime(any(), any(), any()) } returns fakeUi
        every { reminderCoordinator.buildAlarmFromState(any()) } returns null

        coEvery { observeNote.invoke(any()) } returns flowOf()
        coEvery { upsertNote.invoke(any()) } returns 1L
    }

    private fun createVm(): DetailViewModel =
        DetailViewModel(
            savedStateHandle = savedStateHandle,
            observeNote = observeNote,
            upsertNote = upsertNote,
            copyNoteUseCase = copyNoteUseCase,
            archiveNoteUseCase = archiveNoteUseCase,
            setAlarmUseCase = setAlarmUseCase,
            deleteAlarmUseCase = deleteAlarmUseCase,
            saveImageUseCase = saveImageUseCase,
            getPhotoUriUseCase = getPhotoUriUseCase,
            reminderCoordinator = reminderCoordinator
        )

    @Test
    fun loadNote_populates_state_and_textfields() = runTest {
        val stream = MutableSharedFlow<NotoMind?>(replay = 1)
        val existing = NotoMind(title = "Hello", detail = "World")
        stream.tryEmit(existing)
        coEvery { observeNote.invoke(noteId) } returns stream

        vm = createVm()
        advanceUntilIdle()

        val s = vm.state.value
        assertEquals(false, s.isLoading)
        assertEquals(null, s.error)
        assertEquals("Hello", s.title.text.toString())
        assertEquals("World", s.content.text.toString())
        assertEquals(existing, s.note)
    }

    @Test
    fun updateTitle_updates_state_and_calls_upsert() = runTest {
        val stream = MutableSharedFlow<NotoMind?>(replay = 1)
        val existing = NotoMind(title = "Old", detail = "Body")
        stream.tryEmit(existing)
        coEvery { observeNote.invoke(noteId) } returns stream

        var captured: NotoMind? = null
        coEvery { upsertNote.invoke(any()) } answers {
            captured = firstArg()
            100L
        }

        vm = createVm()
        advanceUntilIdle()

        vm.processIntent(DetailIntent.UpdateTitle("New Title"))
        advanceUntilIdle()

        assertEquals("New Title", vm.state.value.note?.title)
        coVerify(exactly = 1) { upsertNote.invoke(any()) }
        assertEquals("New Title", captured?.title)
        assertEquals("Body", captured?.detail)
    }

    @Test
    fun saveNote_applies_text_fields_calls_upsert_and_emits_close() = runTest {
        val stream = MutableSharedFlow<NotoMind?>(replay = 1)
        val existing = NotoMind(title = "Initial", detail = "Initial")
        stream.tryEmit(existing)
        coEvery { observeNote.invoke(noteId) } returns stream

        var lastUpsert: NotoMind? = null
        coEvery { upsertNote.invoke(any()) } answers {
            lastUpsert = firstArg()
            1L
        }

        vm = createVm()
        advanceUntilIdle()

        vm.state.value.title.edit { append(" + Edited") }
        vm.state.value.content.edit { append(" + Edited") }

        vm.effects.test {
            vm.processIntent(DetailIntent.SaveNote)
            advanceUntilIdle()

            assertEquals(DetailEffect.CloseScreen, awaitItem())

            assertEquals("Initial + Edited", lastUpsert?.title)
            assertEquals("Initial + Edited", lastUpsert?.detail)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
