package com.parinexus.main

import androidx.lifecycle.SavedStateHandle
import com.parinexus.domain.IAlarmManager
import com.parinexus.domain.repository.NoteRepository
import com.parinexus.main.navigation.TypeArg
import com.parinexus.model.NotoMind
import com.parinexus.model.NoteType
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var repo: NoteRepository
    private lateinit var alarmManager: IAlarmManager
    private lateinit var notesFlow: MutableStateFlow<List<NotoMind>>
    private lateinit var savedStateHandle: SavedStateHandle

    private val n1 = NotoMind(
        id = 1L, title = "Alpha", detail = "", noteType = NoteType.NOTE,
        isPin = false, selected = false, reminder = -1, interval = -1, color = 0
    )
    private val n2 = n1.copy(id = 2L, title = "Beta")
    private val n3Pinned = n1.copy(id = 3L, title = "Gamma", isPin = true)

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)

        repo = mockk(relaxed = true)
        alarmManager = mockk(relaxed = true)

        notesFlow = MutableStateFlow(listOf(n1, n2, n3Pinned))

        every { repo.getNotePads() } returns notesFlow
        every { repo.getOneNotePad(1L) } returns flowOf(n1)

        coJustRun { repo.upsert(any<List<NotoMind>>()) }
        coJustRun { repo.upsert(any<NotoMind>()) }
        coJustRun { repo.deleteNotePad(any<List<NotoMind>>()) }

        every { repo.timeToString(any<LocalTime>()) } answers {
            val t = firstArg<LocalTime>()
            "%02d:%02d".format(t.hour, t.minute)
        }
        every { repo.dateToString(any<LocalDate>()) } answers {
            firstArg<LocalDate>().toString()
        }

        every {
            alarmManager.setAlarm(
                any<Long>(), any<Long>(), any<Int>(), any<String>(), any<Long>(),any<String>(),
            )
        } just Runs
        every { alarmManager.deleteAlarm(any<Int>()) } just Runs

        savedStateHandle = SavedStateHandle(
            mapOf(TypeArg to NoteType.NOTE.index.toLong())
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    private fun makeVm(): MainViewModel =
        MainViewModel(
            savedStateHandle = savedStateHandle,
            notepadRepository = repo,
            iAlarmManager = alarmManager
        )

    @Test
    fun initEmitsSuccessAndInitialDateUiBuilt() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        val state = vm.mainState.value
        assertIs<MainState.Success>(state)

        val dt = vm.dateTimeState.value
        assertTrue(dt.timeData.isNotEmpty())
        assertTrue(dt.dateData.isNotEmpty())
        assertEquals(dt.timeData.lastIndex, dt.currentTime)
        assertFalse(dt.showDateDialog)
        assertFalse(dt.showTimeDialog)
    }

    @Test
    fun selectAndClearSelected() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.onSelectCard(1L)
        val afterSelect = (vm.mainState.value as MainState.Success).notoMinds
        assertTrue(afterSelect.first { it.id == 1L }.selected)

        vm.clearSelected()
        advanceUntilIdle()
        val afterClear = (vm.mainState.value as MainState.Success).notoMinds
        assertTrue(afterClear.none { it.selected })
    }

    @Test
    fun setPinPinsWhenAnyUnpinnedElseUnpinsAll() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.onSelectCard(1L)
        vm.onSelectCard(3L)
        vm.setPin()
        advanceUntilIdle()

        coVerify {
            repo.upsert(withArg<List<NotoMind>> { list ->
                assertEquals(setOf(1L, 3L), list.mapNotNull { it.id }.toSet())
                assertTrue(list.all { it.isPin })
            })
        }

        notesFlow.value = notesFlow.value.map {
            if (it.id in setOf(1L, 3L)) it.copy(isPin = true) else it
        }
        advanceUntilIdle()

        vm.onSelectCard(1L)
        vm.onSelectCard(3L)
        vm.setPin()
        advanceUntilIdle()

        coVerify {
            repo.upsert(withArg<List<NotoMind>> { list ->
                assertEquals(setOf(1L, 3L), list.mapNotNull { it.id }.toSet())
                assertTrue(list.all { !it.isPin })
            })
        }
    }

    @Test
    fun setAllColorUpdatesSelectedNotes() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.onSelectCard(1L)
        vm.onSelectCard(2L)
        vm.setAllColor(7)
        advanceUntilIdle()

        coVerify {
            repo.upsert(withArg<List<NotoMind>> { list ->
                assertEquals(setOf(1L, 2L), list.mapNotNull { it.id }.toSet())
                assertTrue(list.all { it.color == 7 })
            })
        }
    }

    @Test
    fun setAllArchiveMovesToArchive() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.onSelectCard(1L)
        vm.setAllArchive()
        advanceUntilIdle()

        coVerify {
            repo.upsert(withArg<List<NotoMind>> { list ->
                assertEquals(listOf(1L), list.mapNotNull { it.id })
                assertTrue(list.all { it.noteType == NoteType.ARCHIVE })
            })
        }
    }

    @Test
    fun deleteAlarmClearsReminderAndCancels() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.onSelectCard(1L)
        vm.onSelectCard(3L)
        vm.deleteAlarm()
        advanceUntilIdle()

        coVerify {
            repo.upsert(withArg<List<NotoMind>> { list ->
                assertEquals(setOf(1L, 3L), list.mapNotNull { it.id }.toSet())
                assertTrue(list.all { it.reminder == -1L && it.interval == -1L })
            })
        }
        verify { alarmManager.deleteAlarm(1) }
        verify { alarmManager.deleteAlarm(3) }
    }

    @Test
    fun deleteEmptyNoteRemovesOnlyEmpties() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        notesFlow.value = listOf(
            n1,
            n2.copy(title = "", detail = ""),
            n3Pinned
        )
        vm.deleteEmptyNote()
        advanceUntilIdle()

        coVerify {
            repo.deleteNotePad(withArg<List<NotoMind>> { list ->
                assertEquals(listOf(2L), list.mapNotNull { it.id })
            })
        }
    }

    @Test
    fun dateTimeUiSettersToggleCorrectly() = runTest {
        val vm = makeVm()
        advanceUntilIdle()

        vm.onSetDate(0)
        advanceUntilIdle()
        assertEquals(0, vm.dateTimeState.value.currentDate)
        assertFalse(vm.dateTimeState.value.showDateDialog)

        vm.onSetTime(1)
        advanceUntilIdle()
        assertEquals(1, vm.dateTimeState.value.currentTime)
        assertFalse(vm.dateTimeState.value.showTimeDialog)

        vm.onSetDate(vm.dateTimeState.value.dateData.lastIndex)
        vm.onSetTime(vm.dateTimeState.value.timeData.lastIndex)
        advanceUntilIdle()
        assertTrue(vm.dateTimeState.value.showDateDialog)
        assertTrue(vm.dateTimeState.value.showTimeDialog)

        vm.hideDate()
        vm.hideTime()
        advanceUntilIdle()
        assertFalse(vm.dateTimeState.value.showDateDialog)
        assertFalse(vm.dateTimeState.value.showTimeDialog)
    }
}