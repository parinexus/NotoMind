package com.parinexus.common

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast
import kotlin.test.*

@RunWith(RobolectricTestRunner::class)
class AlarmReceiverTest {

    private lateinit var context: Context
    private lateinit var receiver: AlarmReceiver
    private lateinit var notificationManager: NotificationManager

    private companion object {
        const val CHANNEL_ID = "com.parinexus.notomind.alarm"
        const val TITLE = "My Title"
        const val CONTENT = "My Content"
        const val NOTE_ID = 777L
    }

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        receiver = AlarmReceiver()
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun intent(
        title: String? = TITLE,
        content: String? = CONTENT,
        id: Long = NOTE_ID
    ) = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("title", title)
        putExtra("content", content)
        putExtra("id", id)
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O])
    fun `onReceive on O+ creates channel and posts notification`() {
        receiver.onReceive(context, intent())

        assertEquals("Alarm", ShadowToast.getTextOfLatestToast())

        val channel: NotificationChannel? = notificationManager.getNotificationChannel(CHANNEL_ID)
        assertNotNull(channel, "NotificationChannel should be created on O+")
        assertEquals("NotoMind Notification", channel.name.toString())
        assertEquals("for alarm", channel.description)
        assertEquals(NotificationManager.IMPORTANCE_DEFAULT, channel.importance)
        assertTrue(channel.shouldVibrate())
        assertContentEquals(
            longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 100),
            channel.vibrationPattern
        )

        val posted = Shadows.shadowOf(notificationManager).allNotifications
        assertEquals(1, posted.size, "Exactly one notification should be posted")
        val n: Notification = posted.first()
        assertEquals(CHANNEL_ID, n.channelId)
        assertEquals(TITLE, n.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString())
        assertEquals(CONTENT, n.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1])
    fun `onReceive pre-O does not create channel but posts notification`() {
        receiver.onReceive(context, intent())

        assertEquals("Alarm", ShadowToast.getTextOfLatestToast())

        val posted = Shadows.shadowOf(notificationManager).allNotifications
        assertEquals(1, posted.size)
        val n = posted.first()
        assertEquals(TITLE, n.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString())
        assertEquals(CONTENT, n.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString())
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.O])
    fun `onReceive uses default title and content when blank or null`() {
        receiver.onReceive(context, intent(title = "", content = ""))
        val n1 = Shadows.shadowOf(notificationManager).allNotifications.last()
        assertEquals("Alarm", n1.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString())
        assertEquals("Alarm notification", n1.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString())

        receiver.onReceive(context, intent(title = null, content = null))
        val n2 = Shadows.shadowOf(notificationManager).allNotifications.last()
        assertEquals("Alarm", n2.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString())
        assertEquals("Alarm notification", n2.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString())
    }
}