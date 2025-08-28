package com.parinexus.detail

import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import com.parinexus.domain.ClockProvider
import com.parinexus.domain.NoteFormatter
import com.parinexus.domain.reminder.MS_PER_DAY
import com.parinexus.domain.reminder.indexToInterval
import com.parinexus.domain.reminder.intervalIndex
import com.parinexus.model.NotoMind
import com.parinexus.ui.state.DateDialogUiData
import com.parinexus.ui.state.DateListUiState
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
import java.util.Locale
import javax.inject.Inject
import kotlin.text.get

@OptIn(ExperimentalMaterial3Api::class)
class ReminderCoordinator @Inject constructor(
    private val formatter: NoteFormatter,
    private val clock: ClockProvider
) {
    data class ReminderUi @OptIn(ExperimentalMaterial3Api::class) constructor(
        val dd: DateDialogUiData,
        val datePicker: DatePickerState,
        val timePicker: TimePickerState,
        val today: LocalDateTime,
        val currentLocalDate: LocalDate,
        val timeList: List<LocalTime>
    )

    private fun defaultTimes(): MutableList<LocalTime> = mutableListOf(
        LocalTime(7, 0, 0),
        LocalTime(13, 0, 0),
        LocalTime(19, 0, 0),
        LocalTime(21, 0, 0),
        LocalTime(8, 0, 0) // Pick time (mutable)
    )

    @OptIn(ExperimentalMaterial3Api::class)
    fun init(note: NotoMind): ReminderUi {
        val today = clock.nowLocal()
        val baseDateTime = if (note.reminder > 0) {
            Instant.fromEpochMilliseconds(note.reminder)
                .toLocalDateTime(TimeZone.currentSystemDefault())
        } else today

        val timeList = defaultTimes()
        val timeData = timeList.mapIndexed { index, t ->
            val isPick = index == timeList.lastIndex
            val greater = t > today.time
            DateListUiState(
                title = when (index) {
                    0 -> "Morning"
                    1 -> "Afternoon"
                    2 -> "Evening"
                    3 -> "Night"
                    else -> "Pick time"
                },
                value = if (isPick) formatter.timeToString(baseDateTime.time)
                else formatter.timeToString(t),
                trail = if (!isPick) formatter.timeToString(t) else "",
                isOpenDialog = isPick,
                enable = if (!isPick) greater else true
            )
        }

        val dateData = listOf(
            DateListUiState("Today", "Today", isOpenDialog = false, enable = true),
            DateListUiState("Tomorrow", "Tomorrow", isOpenDialog = false, enable = true),
            DateListUiState("Pick date", formatter.dateToString(baseDateTime.date), isOpenDialog = true, enable = true)
        )

        val intervalData = defaultIntervals()
        val idxFromNote = intervalIndex(note.interval) // باید با ترتیب بالا هم‌راستا باشه
        val safeIntervalIdx = if (idxFromNote in intervalData.indices) idxFromNote else 0

        val dd = DateDialogUiData(
            isEdit = note.reminder > 0,
            currentTime = if (note.reminder > 0) timeData.lastIndex else 0,
            timeData = timeData,
            timeError = today > baseDateTime,
            currentDate = if (note.reminder > 0) dateData.lastIndex else 0,
            dateData = dateData,
            currentInterval = safeIntervalIdx,
            interval = intervalData // ⬅️ مهم
        )

        return ReminderUi(
            dd = dd,
            datePicker = DatePickerState(
                initialSelectedDateMillis = baseDateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
                locale = Locale.getDefault()
            ),
            timePicker = TimePickerState(
                initialHour = baseDateTime.hour,
                initialMinute = baseDateTime.minute,
                is24Hour = false
            ),
            today = today,
            currentLocalDate = baseDateTime.date,
            timeList = timeList
        )
    }

    private fun defaultIntervals(): List<DateListUiState> = listOf(
        DateListUiState(title = "Does not repeat", value = "Does not repeat", isOpenDialog = false, enable = true),
        DateListUiState(title = "Daily",            value = "Daily",            isOpenDialog = false, enable = true),
        DateListUiState(title = "Weekly",           value = "Weekly",           isOpenDialog = false, enable = true),
        DateListUiState(title = "Monthly",          value = "Monthly",          isOpenDialog = false, enable = true),
        DateListUiState(title = "Yearly",           value = "Yearly",           isOpenDialog = false, enable = true),
    )

    @OptIn(ExperimentalMaterial3Api::class)
    fun onSetDateIndex(ui: ReminderUi, index: Int): ReminderUi {
        val dd = ui.dd
        if (index == dd.dateData.lastIndex) {
            return ui.copy(dd = dd.copy(showDateDialog = true))
        }
        val date = if (index == 0) ui.today.date else ui.today.date.plus(1, DateTimeUnit.Companion.DAY)
        val t = ui.timeList[dd.currentTime]
        val candidate = LocalDateTime(date, t)
        val millis = if (index == 0) System.currentTimeMillis() else System.currentTimeMillis() + MS_PER_DAY

        return ui.copy(
            dd = dd.copy(currentDate = index, timeError = candidate < now(), showDateDialog = false),
            datePicker = DatePickerState(initialSelectedDateMillis = millis, locale = Locale.getDefault())
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun onSetTimeIndex(ui: ReminderUi, index: Int): ReminderUi {
        val dd = ui.dd
        return if (index == dd.timeData.lastIndex) {
            ui.copy(dd = dd.copy(showTimeDialog = true))
        } else {
            ui.copy(
                dd = dd.copy(currentTime = index, timeError = false),
                timePicker = TimePickerState(
                    initialHour = ui.timeList[index].hour,
                    initialMinute = ui.timeList[index].minute,
                    is24Hour = false
                )
            )
        }
    }

    fun onSetIntervalIndex(ui: ReminderUi, index: Int): ReminderUi =
        ui.copy(dd = ui.dd.copy(currentInterval = index))

    fun onConfirmDate(ui: ReminderUi, selectedDateMillis: Long?): ReminderUi {
        val dd = ui.dd
        selectedDateMillis ?: return ui.copy(dd = dd.copy(showDateDialog = false))
        val picked = Instant.Companion.fromEpochMilliseconds(selectedDateMillis)
            .toLocalDateTime(TimeZone.Companion.currentSystemDefault())
        val t = ui.timeList[dd.currentTime]
        val candidate = LocalDateTime(picked.date, t)

        val newDateData = dd.dateData.toMutableList().apply {
            val last = lastIndex
            this[last] = this[last].copy(value = formatter.dateToString(picked.date))
        }

        return ui.copy(
            dd = dd.copy(
                dateData = newDateData,
                currentDate = newDateData.lastIndex,
                timeError = (candidate < now()),
                showDateDialog = false
            )
        )
    }

    fun onConfirmTime(ui: ReminderUi, hour: Int, minute: Int): ReminderUi {
        val dd = ui.dd
        val mutableTimes = ui.timeList.toMutableList()
        val picked = LocalTime(hour, minute)
        mutableTimes[mutableTimes.lastIndex] = picked // ذخیره در خانه آخر

        val date = when (dd.currentDate) {
            0 -> ui.today.date
            1 -> ui.today.date.plus(1, DateTimeUnit.Companion.DAY)
            else -> ui.currentLocalDate
        }
        val candidate = LocalDateTime(date, picked)

        val newTimeData = dd.timeData.toMutableList().apply {
            val last = lastIndex
            this[last] = this[last].copy(value = formatter.timeToString(picked))
        }

        return ui.copy(
            dd = dd.copy(
                timeData = newTimeData,
                currentTime = newTimeData.lastIndex,
                timeError = (candidate < now()),
                showTimeDialog = false
            ),
            timeList = mutableTimes
        )
    }

    fun buildAlarmFromState(ui: ReminderUi): Pair<Long, Long?>? {
        val dd = ui.dd
        val time = ui.timeList[dd.currentTime]
        val date = when (dd.currentDate) {
            0 -> ui.today.date
            1 -> ui.today.date.plus(1, DateTimeUnit.Companion.DAY)
            else -> ui.currentLocalDate
        }
        val interval = indexToInterval(dd.currentInterval).millis
        val now = ui.today.toInstant(TimeZone.Companion.currentSystemDefault())
        val setInstant = LocalDateTime(date, time).toInstant(TimeZone.Companion.currentSystemDefault())
        return if (setInstant.toEpochMilliseconds() > now.toEpochMilliseconds()) {
            setInstant.toEpochMilliseconds() to interval
        } else null
    }

    private fun now(): LocalDateTime =
        Clock.System.now().toLocalDateTime(TimeZone.Companion.currentSystemDefault())
}