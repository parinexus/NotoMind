package com.parinexus.domain

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

interface NoteFormatter {
    fun timeToString(time: LocalTime): String
    fun dateToString(date: LocalDate): String
    fun dateToString(millis: Long): String
}
