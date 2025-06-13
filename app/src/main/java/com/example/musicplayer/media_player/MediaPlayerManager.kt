package com.example.musicplayer.media_player

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

class MediaPlayerManager (
    private val context: Context,
    private val onPrepared: ((Int) -> Unit) ? = null,
    private val onCompletion: (() -> Unit) ? = null,
) {
    private var mediaPlayer: MediaPlayer? = null
    private var isPrepared = false

    fun load(uri: Uri) {
        release()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(context, uri)
                prepareAsync()
                setOnPreparedListener { isPrepared = true
                onPrepared?.invoke(it.duration)
                it.start()}

                setOnCompletionListener {
                    onCompletion?.invoke()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun play() {
        if (isPrepared && mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
    }

    fun seekTo(position: Int) {
        if (isPrepared) {
            mediaPlayer?.seekTo(position)
        }
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun getDuration(): Int = mediaPlayer?.duration ?: 0


    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        isPrepared = false
    }
}