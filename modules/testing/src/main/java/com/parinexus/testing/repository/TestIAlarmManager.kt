package com.parinexus.testing.repository

import com.parinexus.domain.IAlarmManager

class TestIAlarmManager : IAlarmManager {
    override fun setAlarm(
        timeInMil: Long,
        interval: Long?,
        requestCode: Int,
        title: String,
        noteId: Long,
        content: String,
    ) {
        TODO("Not yet implemented")
    }

    override fun deleteAlarm(requestCode: Int) {
        TODO("Not yet implemented")
    }
}
