package com.parinexus.detail

import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.snapshotFlow
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.parinexus.domain.IAlarmManager
import com.parinexus.detail.navigation.DetailArg
import com.parinexus.domain.repository.NoteRepository
import com.parinexus.model.NoteCheck
import com.parinexus.model.NoteImage
import com.parinexus.model.NotoMind
import com.parinexus.model.NoteType
import com.parinexus.model.NoteUri
import com.parinexus.ui.state.DateDialogUiData
import com.parinexus.ui.state.DateListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val notePadRepository: NoteRepository,
    private val IAlarmManager: IAlarmManager,
) : ViewModel() {

    // --------------------------------------------------------------------------------------------
    // 🛠 FIX: Make sure date/time state is initialized BEFORE any init{} block or method uses it.
    // In Kotlin, properties initialize in textual order interleaved with init{} blocks. The crash
    // came from calling _dateTimeState.value in initDate() before this property was initialized.
    // So we declare all date/time-related state here at the top, before the init{} block.
    // --------------------------------------------------------------------------------------------

    private val _dateTimeState: MutableStateFlow<DateDialogUiData> =
        MutableStateFlow(DateDialogUiData())
    val dateTimeState = _dateTimeState.asStateFlow()

    private lateinit var currentDateTime: LocalDateTime
    private lateinit var today: LocalDateTime

    private val timeList = mutableListOf(
        LocalTime(7, 0, 0),
        LocalTime(13, 0, 0),
        LocalTime(19, 0, 0),
        LocalTime(20, 0, 0),
        LocalTime(20, 0, 0), // Pick time (mutable)
    )

    @OptIn(ExperimentalMaterial3Api::class)
    var datePicker: DatePickerState = DatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis(),
        locale = Locale.getDefault(),
    )

    @OptIn(ExperimentalMaterial3Api::class)
    var timePicker: TimePickerState = TimePickerState(12, 4, is24Hour = false)

    private var currentLocalDate = LocalDate(1, 2, 3)

    // --------------------------------------------------------------------------------------------

    private val id = savedStateHandle.toRoute<DetailArg>().id
    val note = MutableStateFlow(NotoMind())

    val title = TextFieldState()
    val content = TextFieldState()

    private val _state = MutableStateFlow<DetailState>(DetailState.Loading())
    val state = _state.asStateFlow()
    private var applyingDbUpdate = false
    private var hasLoadedFromDb = false

    init {
        // 1) گوش دادن مداوم به نوت از DB
        viewModelScope.launch {
            var firstLoad = true
            notePadRepository.getOneNotePad(id)
                .filterNotNull()
                .distinctUntilChanged()
                .collectLatest { dbNote ->
                    applyingDbUpdate = true
                    note.update { dbNote }

                    if (firstLoad) {
                        // مقداردهی اولیه‌ی فیلدهای متنی (جایگزینی کامل متن)
                        title.edit { replace(0, length, dbNote.title) }
                        content.edit { replace(0, length, dbNote.detail) }

                        initDate(dbNote)
                        _state.update { DetailState.Success(id) }

                        firstLoad = false
                        hasLoadedFromDb = true // ✅ از اینجا به بعد اجازه‌ی ذخیره داریم
                    }

                    applyingDbUpdate = false
                }
        }

        // 2) پرسیست تغییرات note (با گاردها)
        viewModelScope.launch {
            note
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { n ->
                    if (!applyingDbUpdate && shouldPersist(n)) {
                        saveNote()
                    }
                }
        }

        // 3) سینک عنوان از TextFieldState به note
        viewModelScope.launch {
            snapshotFlow { title.text.toString() }
                .debounce(500)
                .distinctUntilChanged()
                .collectLatest { text ->
                    if (note.value.id != -1L) {
                        note.update { it.copy(title = text) }
                    }
                }
        }

        // 4) سینک محتوا از TextFieldState به note
        viewModelScope.launch {
            snapshotFlow { content.text.toString() }
                .debounce(500)
                .distinctUntilChanged()
                .collectLatest { text ->
                    if (note.value.id != -1L) {
                        note.update { it.copy(detail = text) }
                    }
                }
        }
    }

    private suspend fun saveNote() {
        Timber.d("save note ${'$'}{note.value}")
        if (shouldPersist(note.value)) {
            notePadRepository.upsert(note.value)
        } else {
            Timber.d("skip save: empty title/detail for new note")
        }
    }

    private suspend fun computeUri(notomind: NotoMind) = withContext(Dispatchers.IO) {
        val regex =
            "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)"

        if (notomind.detail.contains(regex.toRegex())) {
            val uri = notomind.detail.split("\\s".toRegex())
                .filter { it.trim().matches(regex.toRegex()) }
                .mapIndexed { index, s ->
                    val path = s.toUri().authority ?: ""
                    val icon = "https://icon.horse/icon/${'$'}path"
                    NoteUri(
                        id = index,
                        icon = icon,
                        path = path,
                        uri = s,
                    )
                }
            // use uri if needed
        }
    }

    fun onCheckChange(text: String, id: Long) {
        val noteChecks = note.value.checks.toMutableList()
        val index = noteChecks.indexOfFirst { it.id == id }
        val noteCheck = noteChecks[index].copy(content = text)
        noteChecks[index] = noteCheck
        note.update { it.copy(checks = noteChecks) }
    }

    fun addCheck() {
        viewModelScope.launch {
            val noteCheck = NoteCheck(isCheck = false)
            val noteChecks = note.value.checks.toMutableList()
            noteChecks.add(noteCheck)
            notePadRepository.upsert(note.value.copy(checks = noteChecks))
            val noteWithCheckId = notePadRepository.getOneNotePad(note.value.id).first()!!
            note.update { noteWithCheckId }
        }
    }

    fun onCheck(check: Boolean, id: Long) {
        val noteChecks = note.value.checks.toMutableList()
        val index = noteChecks.indexOfFirst { it.id == id }
        val noteCheck = noteChecks[index].copy(isCheck = check)
        noteChecks[index] = noteCheck
        note.update { it.copy(checks = noteChecks) }
    }

    fun onCheckDelete(id: Long) {
        val noteChecks = note.value.checks.toMutableList()
        val index = noteChecks.indexOfFirst { it.id == id }
        val noteCheck = noteChecks.removeAt(index)
        note.update { it.copy(checks = noteChecks) }
        viewModelScope.launch { notePadRepository.deleteCheckNote(id, noteCheck.noteId) }
    }

    fun unCheckAllItems() {
        val noteChecks = note.value.checks.map { it.copy(isCheck = false) }
        note.update { it.copy(checks = noteChecks) }
    }

    fun deleteCheckedItems() {
        val checkNote = note.value.checks.filter { it.isCheck }
        val notCheckNote = note.value.checks.filter { !it.isCheck }

        note.update { it.copy(checks = notCheckNote) }
        viewModelScope.launch {
            checkNote.forEach {
                notePadRepository.deleteCheckNote(
                    it.id,
                    it.noteId
                )
            }
        }
    }

    fun hideCheckBoxes() {
        val noteCheck = note.value.checks.joinToString(separator = "\n") { it.content }

        note.update { it.copy(detail = noteCheck, isCheck = false, checks = emptyList()) }
        content.edit { append(noteCheck) }

        viewModelScope.launch { notePadRepository.deleteNoteCheckByNoteId(note.value.id) }
    }

    fun pinNote() {
        note.update { it.copy(isPin = !it.isPin) }
    }

    fun onColorChange(index: Int) {
        note.update { it.copy(color = index) }
    }

    fun onImageChange(index: Int) {
        note.update { it.copy(background = index) }
    }

    fun setAlarm(time: Long, interval: Long?) {
        val noteN = note.value.copy(
            reminder = time,
            interval = interval ?: -1,
            reminderString = notePadRepository.dateToString(time),
        )
        note.update { noteN }

        viewModelScope.launch {
            IAlarmManager.setAlarm(
                time,
                interval,
                requestCode = noteN.id.toInt(),
                title = noteN.title,
                content = noteN.detail,
                noteId = noteN.id,
            )
        }
    }

    fun deleteAlarm() {
        val note2 = note.value.copy(reminder = -1, interval = -1)
        note.update { note2 }
        viewModelScope.launch { IAlarmManager.deleteAlarm(note2.id.toInt()) }
    }

    fun onArchive() {
        val note2 = if (note.value.noteType == NoteType.ARCHIVE) {
            note.value.copy(noteType = NoteType.NOTE)
        } else {
            note.value.copy(noteType = NoteType.ARCHIVE)
        }
        note.update { note2 }
    }

    fun onDelete() {
        note.update { it.copy(noteType = NoteType.TRASH) }
    }

    fun copyNote() {
        viewModelScope.launch {
            var note2 = note.value
            note2 = note2.copy(
                id = -1,
                checks = note2.checks.map { it.copy(id = -1) },
                images = note2.images.map { it.copy(id = -1) },
            )
            notePadRepository.upsert(note2)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun getAudioLength(path: String): Long {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        return try {
            mediaMetadataRetriever.setDataSource(path)
            val time = mediaMetadataRetriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )
            time?.toLong() ?: 1L
        } finally {
            // 🛠 FIX: Avoid resource leak
            mediaMetadataRetriever.release()
        }
    }

    // date and time dialog logic ---------------------------------------------------------------

    private fun initDate(note: NotoMind) {
        val now = Clock.System.now()
        today = now.toLocalDateTime(TimeZone.currentSystemDefault())
        currentDateTime = if (note.reminder > 0) {
            Instant.fromEpochMilliseconds(note.reminder).toLocalDateTime(
                TimeZone.currentSystemDefault(),
            )
        } else {
            today
        }
        currentLocalDate = currentDateTime.date

        Timber.e("initDate → today=${'$'}today, currentDateTime=${'$'}currentDateTime, note.reminder=${'$'}{note.reminder}")

        // Build time options for UI from the shared timeList (last one = pick time)
        val timeDataList = timeList.mapIndexed { index, localTime ->
            val isPick = index == timeList.lastIndex
            val greater = localTime > today.time

            DateListUiState(
                title = when (index) {
                    0 -> "Morning"
                    1 -> "Afternoon"
                    2 -> "Evening"
                    3 -> "Night"
                    else -> "Pick time"
                },
                value = if (isPick) {
                    notePadRepository.timeToString(currentDateTime.time) ?: ""
                } else {
                    notePadRepository.timeToString(localTime) ?: ""
                },
                trail = if (!isPick) notePadRepository.timeToString(localTime) ?: "" else "",
                isOpenDialog = isPick,
                enable = if (!isPick) greater else true,
            )
        }

        val dateDataList = listOf(
            DateListUiState("Today", "Today", isOpenDialog = false, enable = true),
            DateListUiState("Tomorrow", "Tomorrow", isOpenDialog = false, enable = true),
            DateListUiState(
                "Pick date",
                notePadRepository.dateToString(currentDateTime.date) ?: "",
                isOpenDialog = true,
                enable = true,
            ),
        )

        val daily = DateTimeUnit.HOUR.times(24).duration.toLong(DurationUnit.MILLISECONDS)
        val weekly = DateTimeUnit.HOUR.times(24 * 7).duration.toLong(DurationUnit.MILLISECONDS)
        val monthly = DateTimeUnit.HOUR.times(24 * 30).duration.toLong(DurationUnit.MILLISECONDS)
        val yearly = DateTimeUnit.HOUR.times(24 * 365).duration.toLong(DurationUnit.MILLISECONDS)

        val intervalIndex = when (note.interval) {
            daily -> 1
            weekly -> 2
            monthly -> 3
            yearly -> 4
            else -> 0
        }

        _dateTimeState.value = _dateTimeState.value.copy(
            isEdit = note.reminder > 0,
            currentTime = if (note.reminder > 0) timeDataList.lastIndex else 0,
            timeData = timeDataList,
            timeError = today > currentDateTime,
            currentDate = if (note.reminder > 0) dateDataList.lastIndex else 0,
            dateData = dateDataList,
            currentInterval = intervalIndex,
            interval = listOf(
                DateListUiState(
                    "Does not repeat",
                    "Does not repeat",
                    isOpenDialog = false,
                    enable = true
                ),
                DateListUiState("Daily", "Daily", isOpenDialog = false, enable = true),
                DateListUiState("Weekly", "Weekly", isOpenDialog = false, enable = true),
                DateListUiState("Monthly", "Monthly", isOpenDialog = false, enable = true),
                DateListUiState("Yearly", "Yearly", isOpenDialog = false, enable = true),
            ),
        )

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
            _dateTimeState.update { it.copy(showDateDialog = true) }
        } else {
            val date2 = if (index == 0) today.date else today.date.plus(1, DateTimeUnit.DAY)
            val time = timeList[dateTimeState.value.currentTime]
            val localtimedate = LocalDateTime(date2, time)
            _dateTimeState.update {
                it.copy(
                    currentDate = index,
                    timeError = today > localtimedate,
                )
            }
            val dateMillis =
                if (index == 0) System.currentTimeMillis() else System.currentTimeMillis() + 24 * 60 * 60 * 1000
            setDatePicker(dateMillis)
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
            _dateTimeState.update { it.copy(showTimeDialog = true) }
        } else {
            _dateTimeState.update { it.copy(currentTime = index, timeError = false) }
            setTimePicker(timeList[index].hour, timeList[index].minute)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun setTimePicker(hour: Int, minute: Int) {
        timePicker = TimePickerState(hour, minute, false)
    }

    fun onSetInterval(index: Int) {
        _dateTimeState.update { it.copy(currentInterval = index) }
    }

    fun setAlarm() {
        val time = timeList[dateTimeState.value.currentTime]
        val date = when (dateTimeState.value.currentDate) {
            0 -> today.date
            1 -> today.date.plus(1, DateTimeUnit.DAY)
            else -> currentLocalDate
        }

        val daily = DateTimeUnit.HOUR.times(24).duration.toLong(DurationUnit.MILLISECONDS)
        val weekly = DateTimeUnit.HOUR.times(24 * 7).duration.toLong(DurationUnit.MILLISECONDS)
        val monthly = DateTimeUnit.HOUR.times(24 * 30).duration.toLong(DurationUnit.MILLISECONDS)
        val yearly = DateTimeUnit.HOUR.times(24 * 365).duration.toLong(DurationUnit.MILLISECONDS)

        val interval = when (dateTimeState.value.currentInterval) {
            0 -> null
            1 -> daily
            2 -> weekly
            3 -> monthly
            4 -> yearly
            else -> null
        }

        val now = today.toInstant(TimeZone.currentSystemDefault())
        val setime = LocalDateTime(date, time).toInstant(TimeZone.currentSystemDefault())
        if (setime.toEpochMilliseconds() > now.toEpochMilliseconds()) {
            setAlarm(setime.toEpochMilliseconds(), interval)
        } else {
            Timber.w("Alarm not set: selected time is in the past")
        }
    }

    fun hideTime() {
        _dateTimeState.update { it.copy(showTimeDialog = false) }
    }

    fun hideDate() {
        _dateTimeState.update { it.copy(showDateDialog = false) }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun onSetDate() {
        datePicker.selectedDateMillis?.let { timee ->
            val date = Instant.fromEpochMilliseconds(timee)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            currentLocalDate = date.date
            val time = timeList[dateTimeState.value.currentTime]
            val localtimedate = LocalDateTime(currentLocalDate, time)

            _dateTimeState.update {
                val im = it.dateData.toMutableList()
                im[im.lastIndex] =
                    im[im.lastIndex].copy(value = notePadRepository.dateToString(date.date))
                it.copy(
                    dateData = im,
                    currentDate = im.lastIndex,
                    timeError = today > localtimedate,
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun onSetTime() {
        val time = LocalTime(timePicker.hour, timePicker.minute)
        timeList[timeList.lastIndex] = time
        val date = when (dateTimeState.value.currentDate) {
            0 -> today.date
            1 -> today.date.plus(1, DateTimeUnit.DAY)
            else -> currentLocalDate
        }
        val datetime = LocalDateTime(date, time)

        Timber.tag("onSettime").e("current ${'$'}today date ${'$'}datetime")

        _dateTimeState.update {
            val im = it.timeData.toMutableList()
            im[im.lastIndex] = im[im.lastIndex].copy(value = notePadRepository.timeToString(time))
            it.copy(
                timeData = im,
                currentTime = im.lastIndex,
                timeError = datetime < today,
            )
        }
    }

    fun saveImage(uri: String) {
        val id = notePadRepository.saveImage(uri)
        val image = NoteImage(id = id, path = notePadRepository.getImagePath(id))
        note.update { it.copy(images = it.images + image) }
    }

    fun getPhotoUri(): String = notePadRepository.getUri()

    private fun shouldPersist(n: NotoMind): Boolean {
        if (!hasLoadedFromDb) return false
        return !n.isBlankNote()
    }

    private fun NotoMind.isBlankNote(): Boolean {
        return title.isBlank() &&
                detail.isBlank() &&
                checks.isEmpty() &&
                images.isEmpty() &&
                (reminder <= 0)
    }
    private suspend fun discardIfBlank() {
        val n = note.value
        if (n.id != -1L && n.isBlankNote()) {
            // گزینه A) اگر متد حذف سخت داری، ازش استفاده کن و همین‌جا تموم:
            // notePadRepository.deleteNote(n.id)

            // گزینه B) سافت‌دیلیت: به TRASH ببرید تا توی لیست اصلی دیده نشه
            val trashed = n.copy(noteType = NoteType.TRASH)
            note.update { trashed }
            notePadRepository.upsert(trashed)
            Timber.d("Discarded empty note id=${n.id} as TRASH")
        }
    }

    fun onExit() {
        viewModelScope.launch { discardIfBlank() }
    }


}
