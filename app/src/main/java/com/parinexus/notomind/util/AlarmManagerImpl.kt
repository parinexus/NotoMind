package com.parinexus.notomind.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.parinexus.common.AlarmReceiver
import com.parinexus.domain.IAlarmManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AlarmManagerImpl
@Inject constructor(
    @ApplicationContext private val context: Context,
) : IAlarmManager {

    override fun setAlarm(
        timeInMil: Long,
        interval: Long?,
        requestCode: Int,
        title: String,
        noteId: Long,
        content: String,
    ) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.putExtra("title", title)
            intent.putExtra("content", content)
            intent.putExtra("id", noteId)
            PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        if (interval == null) {
            alarmMgr.setExact(
                AlarmManager.RTC_WAKEUP,
                timeInMil,
                alarmIntent,
            )
        } else {
            alarmMgr.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                timeInMil,
                interval,
                alarmIntent,
            )
        }
    }

    override fun deleteAlarm(requestCode: Int) {
        val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE,
            )
        }

        alarmMgr.cancel(alarmIntent)
    }
}