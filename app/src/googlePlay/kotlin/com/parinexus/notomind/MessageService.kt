package com.parinexus.playnotepad

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.RemoteMessage.Notification
import timber.log.Timber
import java.net.URL
import com.parinexus.designsystem.R as Rd

class MessageService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        // اگر پیام notification داشت → نوتیف بساز
        remoteMessage.notification?.let {
            sendNotification(it, this)
        }
    }

    override fun onNewToken(token: String) {
        Timber.tag(TAG).d("Refreshed token: $token")
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        Timber.tag(TAG).d("sendRegistrationTokenToServer $token")
        // اینجا می‌تونی توکن رو به سرور خودت بفرستی
    }

    private fun sendNotification(notification: Notification, context: Context) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        val channelId = "fcm_default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // ✅ اول کانال رو بساز (برای اندروید 8 به بالا)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(Rd.string.modules_designsystem_default_notification_channel_id),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // ✅ بعد Builder رو بساز
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setColor(context.getColor(R.color.primary))
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle(notification.title)
            .setContentText(notification.body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        // اگر پیام تصویر داشت → BigPictureStyle
        notification.imageUrl?.let {
            val bitmap = getBitmap(it.toString())
            notificationBuilder
                .setLargeIcon(bitmap)
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)
                        .bigLargeIcon(null as Bitmap?)
                )
        }

        // نمایش نوتیف
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
        private const val NOTIFICATION_ID = 1001
    }
}

fun getBitmap(uri: String): Bitmap? {
    return try {
        val input = URL(uri).openStream()
        val bitmap = BitmapFactory.decodeStream(input)
        input.close()
        Timber.e("download")
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}