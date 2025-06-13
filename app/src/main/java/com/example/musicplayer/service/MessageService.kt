package com.example.musicplayer.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.example.musicplayer.R
import com.example.musicplayer.screen.splash.SplashActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessageService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.notification != null) {
            val splashIntent = Intent(applicationContext, SplashActivity::class.java)
            splashIntent.putExtra(FROM_NOTI, FROM_NOTI_NEW_FILE)
            splashIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val pendingIntent = PendingIntent.getActivity(applicationContext,
                REQUEST_CODE_NEW_FILE, splashIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

            val notificationBuilder = NotificationCompat.Builder(this, "channel_id")
                .setContentTitle(remoteMessage.notification!!.title)
                .setContentText(remoteMessage.notification!!.body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(NotificationCompat.BigTextStyle())
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setSmallIcon(R.drawable.app_logo)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager



            notificationManager.notify(0, notificationBuilder.build())
        }
    }

    companion object {
        const val FROM_NOTI : String= "FROM_NOTI"
        const val FROM_NOTI_NEW_FILE : Boolean = true
        const val REQUEST_CODE_NEW_FILE = 1000
    }
}
