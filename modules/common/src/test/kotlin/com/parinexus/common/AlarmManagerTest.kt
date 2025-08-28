//package com.parinexus.common
//
//import android.app.AlarmManager as SysAlarm
//import android.app.PendingIntent
//import android.content.Context
//import androidx.test.core.app.ApplicationProvider
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.robolectric.RobolectricTestRunner
//import org.robolectric.Shadows
//import kotlin.test.assertEquals
//import kotlin.test.assertNotNull
//import kotlin.test.assertTrue
//
//@RunWith(RobolectricTestRunner::class)
//class AlarmManagerRobolectricTest {
//
//    private lateinit var context: Context
//    private lateinit var sut: AlarmManagerImpl
//    private lateinit var sysAlarm: SysAlarm
//
//    private companion object {
//        const val WHEN_ONE_SHOT = 1_700_000_000_000L
//        const val WHEN_REPEAT  = 1_700_000_100_000L
//        const val INTERVAL_15M = 15 * 60 * 1000L
//
//        const val REQ_ONE_SHOT = 42
//        const val REQ_REPEAT   = 7
//        const val REQ_CANCEL   = 123
//
//        const val TITLE   = "Reminder title"
//        const val CONTENT = "Reminder content"
//        const val NOTE_ID = 99L
//    }
//
//    @Before
//    fun setUp() {
//        context = ApplicationProvider.getApplicationContext()
//        sut = AlarmManager(context)
//        sysAlarm = context.getSystemService(Context.ALARM_SERVICE) as SysAlarm
//    }
//
//    @Test
//    fun `setAlarm one-shot uses setExact and fills extras`() {
//        sut.setAlarm(
//            timeInMil = WHEN_ONE_SHOT,
//            interval = null,
//            requestCode = REQ_ONE_SHOT,
//            title = TITLE,
//            noteId = NOTE_ID,
//            content = CONTENT
//        )
//
//        val shadowAlarm = Shadows.shadowOf(sysAlarm)
//        val scheduled = shadowAlarm.scheduledAlarms
//        assertEquals(1, scheduled.size, "Expected exactly one scheduled alarm")
//
//        val a = scheduled.first()
//        assertEquals(SysAlarm.RTC_WAKEUP, a.type)
//        assertEquals(WHEN_ONE_SHOT, a.triggerAtTime)
//
//        val op: PendingIntent = a.operation
//        val shadowOp = Shadows.shadowOf(op)
//        val savedIntent = shadowOp.savedIntent
//        assertNotNull(savedIntent, "Saved intent should not be null")
//        assertEquals(AlarmReceiver::class.java.name, savedIntent.component?.className)
//
//        assertEquals(TITLE, savedIntent.getStringExtra("title"))
//        assertEquals(CONTENT, savedIntent.getStringExtra("content"))
//        assertEquals(NOTE_ID, savedIntent.getLongExtra("id", -1))
//
//        val flags = shadowOp.flags
//        assertTrue(flags and PendingIntent.FLAG_IMMUTABLE != 0, "PendingIntent should be immutable")
//
//        assertEquals(0L, a.interval, "Expected non-repeating alarm to have interval=0")
//    }
//
//    @Test
//    fun `setAlarm repeating uses setInexactRepeating with correct interval`() {
//        sut.setAlarm(
//            timeInMil = WHEN_REPEAT,
//            interval = INTERVAL_15M,
//            requestCode = REQ_REPEAT,
//            title = "Repeat",
//            noteId = 1L,
//            content = "Repeating content"
//        )
//
//        val shadowAlarm = Shadows.shadowOf(sysAlarm)
//        val scheduled = shadowAlarm.scheduledAlarms
//        assertEquals(1, scheduled.size, "Expected one scheduled repeating alarm")
//
//        val a = scheduled.first()
//        assertEquals(SysAlarm.RTC_WAKEUP, a.type)
//        assertEquals(WHEN_REPEAT, a.triggerAtTime)
//        assertEquals(INTERVAL_15M, a.interval, "Repeat interval should match")
//        val opIntent = Shadows.shadowOf(a.operation).savedIntent
//        assertEquals(AlarmReceiver::class.java.name, opIntent.component?.className)
//    }
//
//    @Test
//    fun `deleteAlarm cancels matching pending operation`() {
//        sut.setAlarm(
//            timeInMil = WHEN_ONE_SHOT,
//            interval = null,
//            requestCode = REQ_CANCEL,
//            title = "To be canceled",
//            noteId = 3L,
//            content = "c"
//        )
//        val shadowBefore = Shadows.shadowOf(sysAlarm)
//        assertEquals(1, shadowBefore.scheduledAlarms.size, "Alarm should be scheduled before delete")
//
////        sut.deleteAlarm(REQ_CANCEL)
//
//        val shadowAfter = Shadows.shadowOf(sysAlarm)
//        assertEquals(0, shadowAfter.scheduledAlarms.size, "Alarm should be canceled")
//    }
//}