package com.parinexus.domain.usecases

import com.parinexus.domain.IAlarmManager
import com.parinexus.domain.NoteFormatter
import com.parinexus.domain.repository.NoteRepository
import com.parinexus.model.NotoMind
import javax.inject.Inject

class SetAlarmUseCase @Inject constructor(
    private val repo: NoteRepository,
    private val alarm: IAlarmManager,
    private val formatter: NoteFormatter,
) {
    suspend operator fun invoke(note: NotoMind, time: Long, interval: Long?): NotoMind {
        val updated = note.copy(
            reminder = time,
            interval = interval ?: -1,
            reminderString = formatter.dateToString(time)
        )
        repo.upsert(updated)
        alarm.setAlarm(
            timeInMil = time,
            interval = interval,
            requestCode = updated.id.toInt(),
            title = updated.title,
            content = updated.detail,
            noteId = updated.id
        )
        return updated
    }
}
