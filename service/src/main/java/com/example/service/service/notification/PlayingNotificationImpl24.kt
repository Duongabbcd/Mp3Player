package com.example.service.service.notification

import com.example.service.R
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.service.service.MusicService


class PlayingNotificationImpl24 : PlayingNotification() {
    companion object {
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        var bitmapDefault: Icon? = null

//        var activity: AppCompatActivity? =null
    }

    override fun update() {
        stopped = false

        val song = service.getCurrentSong()

        val albumName = song.albumName
        val artistName = song.artistName
        val isPlaying = service.isPlaying()

        val text = if (TextUtils.isEmpty(albumName)) artistName
        else "$artistName - $albumName"

        val playButtonResId = if (isPlaying) R.drawable.icon_mini_pause
        else R.drawable.icon_mini_play

        val serviceName = ComponentName(service, MusicService::class.java)
        val intent = Intent(MusicService.ACTION_QUIT)
        intent.component = serviceName

        val x= service.packageManager.getLaunchIntentForPackage("com.example.musicplayer")
        val customPendingIntent = PendingIntent.getActivity(service, 0 , x, flag)

        val deleteIntent = PendingIntent.getService(
            service, 0, intent,
            flag
        )

        service.runOnUiThread {
            Glide.with(service).asBitmap().load(song.mediaObject?.imageThumb).listener(object :
                RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>,
                    isFirstResource: Boolean
                ): Boolean {
                    update(null, Color.parseColor("#000000"))
                    return true
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    model: Any,
                    target: Target<Bitmap>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    update(resource, Color.parseColor("#000000"))
                    return true
                }

                fun update(bitmap: Bitmap?, color: Int) {
                    val playPauseAction =
                        NotificationCompat.Action(
                            playButtonResId,
                            "Play_Pause",
                            retrievePlaybackAction(MusicService.ACTION_TOGGLE_PAUSE)
                        )
                    val previousAction =
                        NotificationCompat.Action(
                            R.drawable.icon_backward,
                            "Previous",
                            retrievePlaybackAction(MusicService.ACTION_REWIND)
                        )
                    val nextAction =
                        NotificationCompat.Action(
                            R.drawable.icon_forward,
                            "Next",
                            retrievePlaybackAction(MusicService.ACTION_SKIP)
                        )
                    val quitAction =
                        NotificationCompat.Action(
                            R.drawable.icon_clear,
                            "Close",
                            retrievePlaybackAction(MusicService.ACTION_QUIT)
                        )


                    val builder =
                        NotificationCompat.Builder(
                            service,
                            NOTIFICATION_CHANNEL_ID
                        )
                            .setSmallIcon(R.drawable.icon_music_app)
                            .setContentIntent(customPendingIntent)
                            .setLargeIcon(bitmapDefault)
                            .setDeleteIntent(deleteIntent)
                            .setContentTitle(song.mediaObject?.title)
                            .setContentText(text)
                            .setOngoing(true)
                            .setShowWhen(true)
                            .addAction(previousAction)
                            .addAction(playPauseAction)
                            .addAction(nextAction)
                            .addAction(quitAction)
                            .setStyle(
                                androidx.media.app.NotificationCompat.MediaStyle()
                                    .setMediaSession(
                                        service.getMediaSession().sessionToken
                                    )
                                    .setShowActionsInCompactView(0, 1, 2, 3)
                            )
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            .setColor(color)
                    if (stopped) return // notification has been stopped before loading was finished

                    updateNotifyModeAndPostNotification(builder.build())
                }
            }).submit()
        }
    }

    private fun retrievePlaybackAction(action: String): PendingIntent {
        val serviceName = ComponentName(service, MusicService::class.java)
        val intent = Intent(action)
        intent.component = serviceName

        return PendingIntent.getService(service, 0, intent, flag)
    }

}