package com.parinexus.main

import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parinexus.domain.IAlarmManager
import com.parinexus.domain.repository.NoteRepository
import com.parinexus.main.navigation.MainArg
import com.parinexus.model.NoteType
import com.parinexus.ui.state.DateDialogUiData
import com.parinexus.ui.state.DateListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import kotlin.time.DurationUnit

@OptIn(FlowPreview::class)
@HiltViewModel
internal class MainViewModel
@Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val notepadRepository: NoteRepository,
    private val iAlarmManager: IAlarmManager,
) : ViewModel() {

    val searchState = TextFieldState()
    private val mainArg = MainArg(savedStateHandle)
    private val _mainState = MutableStateFlow<MainState>(MainState.Loading)
    val mainState = _mainState.asStateFlow()

    init {

        viewModelScope.launch {
            combine(
                mainState,
                snapshotFlow { searchState.text }
                    .debounce(500),
                notepadRepository.getNotePads(),
            ) { mainState, search, notoMind ->
                Triple(mainState, search, notoMind)
            }.collectLatest { triple ->

                val (mainState, search, notoMind) = triple
                if (mainState is MainState.Success) {
                    if (mainState.isSearch) {
                        when {
                            mainState.searchSort != null -> {
                                var list = when (val searchsort = mainState.searchSort) {
                                    is SearchSort.Color -> {
                                        notoMind.filter { it.color == searchsort.colorIndex }
                                    }
                                    is SearchSort.Label -> {
                                        notoMind.filter { it.labels.any { it.id == searchsort.id } }
                                    }
                                    is SearchSort.Type -> {
                                        when (searchsort.index) {
                                            0 -> notoMind.filter { it.reminder > 0 }
                                            1 -> notoMind.filter { it.isCheck }
                                            2 -> notoMind.filter { it.images.isNotEmpty() }
                                            3 -> notoMind.filter { it.uris.isNotEmpty() }
                                            else -> notoMind
                                        }
                                    }

                                    null -> TODO()
                                }

                                if (search.isNotBlank()) {
                                    list = list.filter { it.toString().contains(search, true) }
                                }

                                _mainState.update {
                                    getSuccess().copy(notoMinds = list)
                                }
                            }
                            search.isNotBlank() -> {

                                val list = notoMind.filter { it.toString().contains(search, true) }
                                _mainState.update {
                                    getSuccess().copy(notoMinds = list)
                                }
                            }

                            else -> {
                                _mainState.value = getSuccess().copy(
                                    notoMinds = emptyList(),
                                )
                            }
                        }
                    } else {

                        val list =
                            when (mainArg.noteType) {
                                NoteType.LABEL -> {
                                    notoMind.filter { it.labels.any { it.id == mainArg.type } }
                                }

                                NoteType.REMAINDER -> {
                                    notoMind.filter { it.reminder > 0 }
                                }

                                else -> {
                                    notoMind.filter {
                                        it.noteType == mainArg.noteType
                                    }
                                }
                            }
                        _mainState.value = getSuccess().copy(
                            notoMinds = list,
                        )
                    }
                } else {

                    _mainState.value = MainState.Success(
                        notoMinds = emptyList(),
                    )

                    initDate()
                }
            }
        }
    }

    /**
     * Handles the selection/deselection of a notoMind card.
     *
     * This function is triggered when a user selects or deselects a notoMind card.
     * It updates the selected state of the corresponding notoMind in the list and
     * updates the UI state accordingly.
     *
     * @param id The ID of the notoMind card that was selected or deselected.
     */
    fun onSelectCard(id: Long) {
        val listNOtePad = getSuccess().notoMinds.toMutableList()
        val index = listNOtePad.indexOfFirst { it.id == id }
        val notoMind = listNOtePad[index]
        val newNotepad = notoMind.copy(selected = !notoMind.selected)

        listNOtePad[index] = newNotepad

        _mainState.value = getSuccess().copy(notoMinds = listNOtePad.toImmutableList())
    }

    fun clearSelected() {
        val listNOtePad =
            getSuccess().notoMinds.map { it.copy(selected = false) }
        _mainState.value = getSuccess().copy(notoMinds = listNOtePad.toImmutableList())
    }

    fun setNoteType(noteType: NoteType) {
        _mainState.value = MainState.Success(noteType = noteType)
    }

    fun setPin() {
        val selectedNotepad =
            getSuccess().notoMinds.filter { it.selected }

        clearSelected()

        if (selectedNotepad.any { !it.isPin }) {
            val pinNotepad = selectedNotepad.map { it.copy(isPin = true) }

            viewModelScope.launch {
                notepadRepository.upsert(pinNotepad)
            }
        } else {
            val unPinNote = selectedNotepad.map { it.copy(isPin = false) }

            viewModelScope.launch {
                notepadRepository.upsert(unPinNote)
            }
        }
    }

    private fun setAlarm(time: Long, interval: Long?) {
        val selectedNotes =
            getSuccess().notoMinds.filter { it.selected }

        clearSelected()
        val notes = selectedNotes.map { it.copy(reminder = time, interval = interval ?: -1) }

        viewModelScope.launch {
            notepadRepository.upsert(notes)
        }

        viewModelScope.launch {
            notes.forEach {
                iAlarmManager.setAlarm(
                    time,
                    interval,
                    requestCode = it.id?.toInt() ?: -1,
                    title = it.title,
                    content = it.detail,
                    noteId = it.id ?: 0L,
                )
            }
        }
    }

    fun deleteAlarm() {
        val selectedNotes =
            getSuccess().notoMinds.filter { it.selected }

        clearSelected()
        val notes = selectedNotes.map { it.copy(reminder = -1, interval = -1) }

        viewModelScope.launch {
            notepadRepository.upsert(notes)
        }

        viewModelScope.launch {
            notes.forEach {
                iAlarmManager.deleteAlarm(it.id?.toInt() ?: 0)
            }
        }
    }

    fun setAllColor(colorId: Int) {
        val selectedNotes =
            getSuccess().notoMinds.filter { it.selected }

        clearSelected()
        val notes = selectedNotes.map { it.copy(color = colorId) }

        viewModelScope.launch {
            notepadRepository.upsert(notes)
        }
    }

    fun setAllArchive() {
        val selectedNotes =
            getSuccess().notoMinds.filter { it.selected }

        clearSelected()
        val notes = selectedNotes.map { it.copy(noteType = NoteType.ARCHIVE) }

        viewModelScope.launch {
            notepadRepository.upsert(notes)
        }
    }

    fun copyNote() {
        viewModelScope.launch(Dispatchers.IO) {
            val id = getSuccess().notoMinds.single { it.selected }.id
            val notepads = notepadRepository.getOneNotePad(id).first()

            if (notepads != null) {
                val copy = notepads.copy(id = -1)

                notepadRepository.upsert(copy)
            }
        }
    }

    fun deleteLabel() {
//        val labelId = (getSuccess().noteType).id
//
//        _mainState.value = getSuccess().copy(noteType = NoteTypeUi())
//
//        viewModelScope.launch {
//            labelRepository.delete(labelId)
//            // noteLabelRepository.deleteByLabelId(labelId)
//        }
    }

    fun renameLabel(name: String) {
//        val labelId = (getSuccess().noteType).id
//
//        viewModelScope.launch {
//            labelRepository.upsert(listOf(Label(labelId, name)))
//        }
    }

    fun deleteEmptyNote() {
        viewModelScope.launch(Dispatchers.IO) {
            val emptyList = notepadRepository.getNotePads().first()
                .filter { it.isEmpty() }

            if (emptyList.isNotEmpty()) {
                notepadRepository.deleteNotePad(emptyList)
            }
        }
    }

    private val _dateTimeState = MutableStateFlow(DateDialogUiData())
    val dateTimeState = _dateTimeState.asStateFlow()
    private lateinit var currentDateTime: LocalDateTime
    private lateinit var today: LocalDateTime
    private val timeListDefault = mutableListOf(
        LocalTime(7, 0, 0),
        LocalTime(13, 0, 0),
        LocalTime(19, 0, 0),
        LocalTime(20, 0, 0),
        LocalTime(20, 0, 0),
    )

    @OptIn(ExperimentalMaterial3Api::class)
    var datePicker: DatePickerState = DatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis(),
        locale = Locale.getDefault(),
    )

    @OptIn(ExperimentalMaterial3Api::class)
    var timePicker: TimePickerState = TimePickerState(12, 4, is24Hour = false)
    private lateinit var currentLocalDate: LocalDate

    // date and time dialog logic

    private fun initDate() {
        val now = Clock.System.now()
        today = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val today2 =
            now.plus(10, DateTimeUnit.MINUTE).toLocalDateTime(TimeZone.currentSystemDefault())
        currentDateTime = today2
        currentLocalDate = currentDateTime.date
        Timber.tag("current date").e(currentLocalDate.toString())

        val timeList = mutableListOf(
            DateListUiState(
                title = "Morning",
                value = "7:00 AM",
                trail = "7:00 AM",
                isOpenDialog = false,
                enable = true,
            ),
            DateListUiState(
                title = "Afternoon",
                value = "1:00 PM",
                trail = "1:00 PM",
                isOpenDialog = false,
                enable = true,
            ),
            DateListUiState(
                title = "Evening",
                value = "7:00 PM",
                trail = "7:00 PM",
                isOpenDialog = false,
                enable = true,
            ),
            DateListUiState(
                title = "Night",
                value = "8:00 PM",
                trail = "8:00 PM",
                isOpenDialog = false,
                enable = true,
            ),
            DateListUiState(
                title = "Pick time",
                value = "1:00 PM",
                isOpenDialog = true,
                enable = true,
            ),

        ).mapIndexed { index, dateListUiState ->
            if (index != timeListDefault.lastIndex) {
                val greater = timeListDefault[index] > today.time
                dateListUiState.copy(
                    enable = greater,
                    value = notepadRepository.timeToString(timeListDefault[index]),
                    trail = notepadRepository.timeToString(timeListDefault[index]),
                )
            } else {
                timeListDefault[timeListDefault.lastIndex] = currentDateTime.time
                dateListUiState.copy(value = notepadRepository.timeToString(currentDateTime.time))
            }
        }.toImmutableList()
        val datelist = listOf(
            DateListUiState(
                title = "Today",
                value = "Today",
                isOpenDialog = false,
                enable = true,
            ),
            DateListUiState(
                title = "Tomorrow",
                value = "Tomorrow",
                isOpenDialog = false,
                enable = true,
            ),
            DateListUiState(
                title = "Pick date",
                value = notepadRepository.dateToString(currentDateTime.date),
                isOpenDialog = true,
                enable = true,
            ),
        ).toImmutableList()
        val interval = 0

        _dateTimeState.update {
            it.copy(
                isEdit = false,
                currentTime = timeList.lastIndex,
                timeData = timeList,
                timeError = today > currentDateTime,
                currentDate = 0,
                dateData = datelist,
                currentInterval = interval,
                interval = listOf(
                    DateListUiState(
                        title = "Does not repeat",
                        value = "Does not repeat",
                        isOpenDialog = false,
                        enable = true,
                    ),
                    DateListUiState(
                        title = "Daily",
                        value = "Daily",
                        isOpenDialog = false,
                        enable = true,
                    ),
                    DateListUiState(
                        title = "Weekly",
                        value = "Weekly",
                        isOpenDialog = false,
                        enable = true,
                    ),
                    DateListUiState(
                        title = "Monthly",
                        value = "Monthly",
                        isOpenDialog = false,
                        enable = true,
                    ),
                    DateListUiState(
                        title = "Yearly",
                        value = "Yearly",
                        isOpenDialog = false,
                        enable = true,
                    ),
                ).toImmutableList(),
            )
        }
        setDatePicker(
            currentDateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
        )
        setTimePicker(
            hour = currentDateTime.hour,
            minute = currentDateTime.minute,
        )
    }

    fun onSetDate(index: Int) {
        if (index == dateTimeState.value.dateData.lastIndex) {
            _dateTimeState.update {
                it.copy(
                    showDateDialog = true,
                )
            }
        } else {
            val date2 = if (index == 0) today.date else today.date.plus(1, DateTimeUnit.DAY)
            val time = timeListDefault[dateTimeState.value.currentTime]
            val localtimedate = LocalDateTime(date2, time)
            _dateTimeState.update {
                it.copy(
                    currentDate = index,
                    timeError = today > localtimedate,
                )
            }
            val date = if (index == 0) {
                System.currentTimeMillis()
            } else {
                System.currentTimeMillis() + 24 * 60 * 60 * 1000
            }
            setDatePicker(date)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun setDatePicker(date: Long) {
        datePicker = DatePickerState(
            initialSelectedDateMillis = date,
            locale = Locale.getDefault(),
        )
    }

    fun onSetTime(index: Int) {
        if (index == dateTimeState.value.timeData.lastIndex) {
            _dateTimeState.update {
                it.copy(
                    showTimeDialog = true,
                )
            }
        } else {
            _dateTimeState.update {
                it.copy(
                    currentTime = index,
                    timeError = false,
                )
            }
            setTimePicker(
                timeListDefault[index].hour,
                timeListDefault[index].minute,
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun setTimePicker(hour: Int, minute: Int) {
        timePicker = TimePickerState(hour, minute, false)
    }

    fun onSetInterval(index: Int) {
        _dateTimeState.update {
            it.copy(currentInterval = index)
        }
    }

    fun setAlarm() {
        val time = timeListDefault[dateTimeState.value.currentTime]
        val date = when (dateTimeState.value.currentDate) {
            0 -> today.date
            1 -> today.date.plus(1, DateTimeUnit.DAY)
            else -> currentLocalDate
        }
        val interval = when (dateTimeState.value.currentInterval) {
            0 -> null
            1 -> DateTimeUnit.HOUR.times(24).duration.toLong(DurationUnit.MILLISECONDS)

            2 -> DateTimeUnit.HOUR.times(24 * 7).duration.toLong(DurationUnit.MILLISECONDS)

            3 -> DateTimeUnit.HOUR.times(24 * 7 * 30).duration.toLong(DurationUnit.MILLISECONDS)

            else -> DateTimeUnit.HOUR.times(24 * 7 * 30).duration.toLong(DurationUnit.MILLISECONDS)
        }

        val setime = LocalDateTime(date, time)
        if (setime > today) {
            setAlarm(
                setime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
                interval,
            )
        } else {}
    }

    fun hideTime() {
        _dateTimeState.update {
            it.copy(showTimeDialog = false)
        }
    }

    fun hideDate() {
        _dateTimeState.update {
            it.copy(showDateDialog = false)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun onSetDate() {
        datePicker.selectedDateMillis?.let { timee ->
            val date = Instant.fromEpochMilliseconds(timee)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            currentLocalDate = date.date
            val time = timeListDefault[dateTimeState.value.currentTime]
            val localtimedate = LocalDateTime(currentLocalDate, time)

            _dateTimeState.update {
                val im = it.dateData.toMutableList()
                im[im.lastIndex] =
                    im[im.lastIndex].copy(value = notepadRepository.dateToString(date.date))
                it.copy(
                    dateData = im.toImmutableList(),
                    currentDate = im.lastIndex,
                    timeError = today > localtimedate,
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun onSetTime() {
        val time = LocalTime(timePicker.hour, timePicker.minute)

        timeListDefault[timeListDefault.lastIndex] = time
        val date = when (dateTimeState.value.currentDate) {
            0 -> today.date
            1 -> today.date.plus(1, DateTimeUnit.DAY)
            else -> currentLocalDate
        }
        val datetime = LocalDateTime(date, time)

        Log.e("onSettime", "current $today date $datetime")

        _dateTimeState.update {
            val im = it.timeData.toMutableList()
            im[im.lastIndex] = im[im.lastIndex].copy(value = notepadRepository.timeToString(time))
            it.copy(
                timeData = im.toImmutableList(),
                currentTime = im.lastIndex,
                timeError = datetime < today,
            )
        }
    }

    private fun getSuccess() = mainState.value as MainState.Success

    fun onSetSearch(searchSort: SearchSort?) {
        _mainState.update {
            getSuccess().copy(
                searchSort = searchSort,
            )
        }
    }
}
