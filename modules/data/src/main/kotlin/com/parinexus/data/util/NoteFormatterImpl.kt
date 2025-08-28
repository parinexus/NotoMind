package com.parinexus.data.util

import com.parinexus.domain.NoteFormatter
import com.parinexus.domain.repository.NoteRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import javax.inject.Inject

class NoteFormatterImpl @Inject constructor(
    private val notePadRepository: NoteRepository
) : NoteFormatter {
    override fun timeToString(time: LocalTime): String =
        notePadRepository.timeToString(time) ?: ""

    override fun dateToString(date: LocalDate): String =
        notePadRepository.dateToString(date) ?: ""

    override fun dateToString(millis: Long): String =
        notePadRepository.dateToString(millis) ?: ""
}