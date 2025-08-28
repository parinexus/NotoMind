package com.parinexus.domain

interface AlarmManager {
    fun setAlarm(
        timeInMil: Long,
        interval: Long?,
        requestCode: Int = 0,
        title: String,
        noteId: Long,
        content: String,
    )

    fun deleteAlarm(requestCode: Int = 0)
}