package com.example.musicplayer.base

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.screen.home.MainActivity
import com.example.musicplayer.screen.player.PlayerActivity
import com.example.musicplayer.utils.Common
import com.example.musicplayer.utils.Constant
import com.example.service.model.Audio
import com.example.service.model.Playlist
import com.example.service.service.MusicPlayerRemote
import com.example.service.service.MusicService
import com.example.service.service.SongLoader
import com.example.service.utils.MusicUtil
import com.example.service.utils.PlaylistUtils
import com.example.service.utils.RemoveOrRenameFile
import com.example.service.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.abs

typealias Inflate<T> = (LayoutInflater) -> T

abstract class BaseActivity<T : ViewBinding>(private val inflater: Inflate<T>) :
    AppCompatActivity() {
    protected val binding: T by lazy { inflater(layoutInflater) }
    val removeOrRenameFile = RemoveOrRenameFile()
    val deleteResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            sendBroadcast(Intent(Constant.ACTION_FINISH_DOWNLOAD))
            if (result.resultCode == RESULT_OK) {
                MusicPlayerRemote.checkAfterDeletePlaying()
                removeAudioNotExist(this)
            }
        }

    private lateinit var progressBar: ProgressBar

    private var totalSeconds = 0
    private var currentSecond = 0L
    private var isPlaying = false

    private var progressJob: Job? = null

    private var previousAudio : Audio = Audio.EMPTY_SONG

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                MusicService.META_CHANGED, MusicService.PLAY_STATE_CHANGED -> {
                    setUpMiniPlayer()
                }

                ACTION_RECREATE -> {
                    MainActivity.isChangeTheme = false
                    recreate()
                }

                ACTION_DELETE_FILE -> {
                    try {
                        val path = intent.getStringExtra(KEY_ID_SONG)
                        val deletedAudio = SongLoader.getAllSongs(this@BaseActivity)
                            .find { it.mediaObject!!.path == path }
                        val id = deletedAudio?.mediaObject?.id?.toIntOrNull() ?: return
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            val pendingIntent = MediaStore.createDeleteRequest(
                                contentResolver, mutableListOf(MusicUtil.getSongFileUri(id))
                            )
                            deleteResultLauncher.launch(
                                IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                            )
                        } else {
                            removeOrRenameFile.deleteAudio(this@BaseActivity, id)
                            MusicPlayerRemote.checkAfterDeletePlaying()
                            removeAudioNotExist(this@BaseActivity)
                        }
                        sendBroadcast(Intent(Utils.ACTION_FINISH_DOWNLOAD))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                ACTION_RENAME_FILE -> {
                    val id = intent.getIntExtra(KEY_ID_SONG, 0)
                    val pathSong = intent.getStringExtra(KEY_PATH_SONG) ?: ""
                    val newName = intent.getStringExtra(KEY_NAME_SONG) ?: ""
                    removeOrRenameFile.renameAudio(this@BaseActivity, id, pathSong, newName)
                }
            }

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        val isDarkMode = true
        val flag: Int
        println("isDarkMode $isDarkMode")
        Common.setLocale(this@BaseActivity, Common.getPreLanguage(this))
        if (isDarkMode) {
            setTheme(R.style.MusicPlayerTheme)
            flag =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        } else {
            setTheme(R.style.MusicPlayerTheme)
            flag =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = flag

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if ((visibility and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                CoroutineScope(Dispatchers.Main).launch {
                    delay(1000)
                    window.decorView.systemUiVisibility = (
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            )

                    val isInDarkMode = true
                    val fl = if (isInDarkMode) {
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    } else {
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    }
                    window.decorView.systemUiVisibility = fl
                }
            }
        }

        window.navigationBarColor = Color.TRANSPARENT
        window.statusBarColor = Color.TRANSPARENT
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        setUpMiniPlayer()
        intentFilter.addAction(MusicService.PLAY_STATE_CHANGED)
        intentFilter.addAction(ACTION_DELETE_FILE)
        intentFilter.addAction(ACTION_RECREATE)
        intentFilter.addAction(ACTION_RENAME_FILE)
        intentFilter.addAction(MusicService.META_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                broadcastReceiver, intentFilter, RECEIVER_EXPORTED
            )
        } else {
            ContextCompat.registerReceiver(
                this,
                broadcastReceiver,
                intentFilter,
                ContextCompat.RECEIVER_EXPORTED
            )
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(broadcastReceiver)
    }

    protected open fun setUpMiniPlayer() {
        try {
            val imageSongAvt = findViewById<ImageView>(R.id.miniAvatar)
            val imagePlayPause = findViewById<ImageView>(R.id.miniPlayBtn)
            val textSongTitle = findViewById<TextView>(R.id.miniSongName)
            val textArtist = findViewById<TextView>(R.id.miniArtist)
            val nextButton = findViewById<ImageView>(R.id.miniNextBtn)
            val prevButton = findViewById<ImageView>(R.id.miniPrevBtn)
            val layoutMiniPlayer = findViewById<ConstraintLayout>(R.id.layout_mini_player)
            progressBar = findViewById(R.id.progress)

            val res: Int
//            val rotateAnimation: ObjectAnimator by lazy { createRotateAnimation(imageSongAvt) }
            if (MusicPlayerRemote.isPlaying()) {
                res = R.drawable.icon_mini_pause
//                rotateAnimation.resume()
            } else {
                res = R.drawable.icon_mini_play
//                rotateAnimation.pause()
                pauseProgress()
            }
            imagePlayPause.setImageResource(res)

            val currentSong = MusicPlayerRemote.getCurrentSong()
            if(previousAudio != currentSong) {
                previousAudio = currentSong
                val songArt =
                    Utils.getAlbumArt(
                        currentSong.mediaObject?.path ?: ""
                    )
                println("currentAudio: $songArt")

                Glide.with(this).load(songArt)
                    .placeholder(R.drawable.app_logo).error(R.drawable.app_logo)
                    .into(imageSongAvt)
            }

            println("getCurrentSong 123: $currentSong")
            if (currentSong == Audio.EMPTY_SONG || currentSong.mediaObject?.path.isNullOrEmpty()) {
                findViewById<View>(R.id.layout_mini_player).isVisible = false
                return
            }
            findViewById<View>(R.id.layout_mini_player).isVisible = true


            totalSeconds = MusicPlayerRemote.getSongDurationMillis()
            progressBar.max = totalSeconds

            startOrResumeProgress()

            val artist = if (!currentSong.artistName[0].isLetter() || currentSong.artistName.contains(
                    Constant.UNKNOWN_STRING,
                    true
                )
            ) this.resources.getString(R.string.unknown_artist) else currentSong.artistName

            println("Song name: ${currentSong.mediaObject?.title} and $artist")
            textSongTitle.text =
                currentSong.mediaObject?.title ?: resources.getString(R.string.unknown_artist)
            textSongTitle.isSelected = true
            textArtist.text = artist

            imagePlayPause.setOnClickListener {
                if (MusicPlayerRemote.isPlaying()) {
                    MusicPlayerRemote.pauseSong()
                } else {
                    MusicPlayerRemote.resumePlaying()
                }
                setUpMiniPlayer()
            }

            layoutMiniPlayer.setOnClickListener {
                startActivity(Intent(this@BaseActivity, PlayerActivity::class.java))
                overridePendingTransition(R.anim.enter_from_bot, R.anim.fade_out)
            }
            layoutMiniPlayer.setOnSWipeListener(this@BaseActivity)

            nextButton.setOnClickListener {
                MusicPlayerRemote.playNextSong()
            }
            prevButton.setOnClickListener {
                MusicPlayerRemote.playPreviousSong()
            }
        } catch (e: Exception) {
            Log.e("BaseActivity", "exception: $e")
        }
    }

    private fun startOrResumeProgress() {
        progressJob?.cancel()
        progressJob = CoroutineScope(Dispatchers.Main).launch {
            while (MusicPlayerRemote.isPlaying()) {
                val position = MusicPlayerRemote.getSongProgressMillis()
                progressBar.progress = position.toInt()
                delay(1000L)
            }
        }
    }


    private fun pauseProgress() {
        isPlaying = false

        // Cancel the job to stop progress
        progressJob?.cancel()
        progressJob = null
    }

    override fun onDestroy() {
        super.onDestroy()
        progressJob?.cancel()
    }

    class SwipeGestureListener(private val activity: Activity) :
        GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            try {
                val diffY = e2.y.minus(e1!!.y)
                val diffX = e2.x.minus(e1.x)

                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight()
                        } else {
                            onSwipeLeft()
                        }
                        return true
                    }
                } else if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY <= 0) {
                        onSwipeUp()
                    }
                    return true
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            return false
        }

        private fun onSwipeUp() {
            activity.startActivity(Intent(activity, PlayerActivity::class.java))
            activity.overridePendingTransition(R.anim.enter_from_bot, R.anim.fade_out)
        }

        private fun onSwipeLeft() {
            MusicPlayerRemote.playNextSong()
        }

        private fun onSwipeRight() {
            MusicPlayerRemote.playPreviousSong()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun View.setOnSWipeListener(activity: Activity) {
        val gestureDetector = GestureDetector(activity, SwipeGestureListener(activity))
        this.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
    }

    open fun openFragment(fragment: Fragment) {
        println("fragment: $fragment")
        val manager: FragmentManager = supportFragmentManager
        val transaction: FragmentTransaction = manager.beginTransaction()
        transaction.replace(R.id.frameLayout, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun createRotateAnimation(playingSongIcon: ImageView): ObjectAnimator {
        return ObjectAnimator.ofFloat(playingSongIcon, "rotation", 0f, 360f).apply {
            duration = 10000 //Time for each completed round.
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.RESTART
            interpolator = LinearInterpolator()
            start()
        }

    }


    companion object {
        fun removeAudioNotExist(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                //modify in playlist
                val playLists: List<Playlist>? =
                    PlaylistUtils.getInstance(context)?.getPlaylists()
                playLists?.let {
                    it.forEach { playlist ->
                        var isChange = false
                        val tempTrack = mutableListOf<Audio>()
                        tempTrack.addAll(playlist.tracks)
                        tempTrack.forEach { audio ->
                            if (audio.mediaObject?.path?.let { it1 -> File(it1).exists() } == false) {
                                isChange = true
                                playlist.tracks.remove(audio)
                            }
                        }
                        if (isChange) {
                            PlaylistUtils.getInstance(context = context)
                                ?.savePlaylist(playlist)
                        }
                    }
                }
            }
        }

        const val ACTION_DELETE_FILE = "ACTION_DELETE_FILE"
        const val ACTION_RENAME_FILE = "ACTION_RENAME_FILE"
        const val KEY_ID_SONG = "KEY_ID_SONG"
        const val KEY_PATH_SONG = "KEY_PATH_SONG"
        const val KEY_NAME_SONG = "KEY_NAME_SONG"
        const val ACTION_RECREATE = "ACTION_RECREATE"
    }
}