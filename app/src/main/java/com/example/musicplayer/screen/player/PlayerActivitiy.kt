package com.example.musicplayer.screen.player

import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivityPlayerBinding
import com.example.musicplayer.screen.home.MainActivity
import com.example.musicplayer.screen.playlist.PlaylistActivity.Companion.ACTION_UPDATE_DATA_PLAYLIST
import com.example.musicplayer.utils.Constant
import com.example.musicplayer.utils.Utils
import com.example.musicplayer.utils.Utils.blurBitmap
import com.example.musicplayer.utils.Utils.setOnSWipeListener
import com.example.service.model.Audio
import com.example.service.service.MusicPlayerRemote
import com.example.service.service.MusicService
import com.example.service.utils.PlaylistUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.musicplayer.base.BaseActivity
import com.example.musicplayer.media_player.MediaPlayerManager
import com.example.musicplayer.screen.player.bottomsheet.AddPlaylistBottomSheet
import com.example.musicplayer.screen.player.bottomsheet.CountDownBottomSheet
import com.example.musicplayer.screen.player.bottomsheet.PlayingBottomSheet
import com.example.musicplayer.screen.player.bottomsheet.TimeSleepBottomSheet
import com.example.musicplayer.screen.playing_queue.PlayingQueueActivity
import com.example.musicplayer.utils.Common.gone
import com.example.musicplayer.utils.Common.visible

class PlayerActivity : BaseActivity<ActivityPlayerBinding>(ActivityPlayerBinding::inflate) {
    private val broadcastPlayerMusic = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            println("PlayerActivity intent.action: $action")
            when (action) {
                MusicService.META_CHANGED, MusicService.PLAY_STATE_CHANGED -> {
                    setUpPlayer()
                }

                MusicService.NEW_SONG_CHANGED -> {
                    updateImage()
                }

                MusicService.ACTION_UPDATE_SLEEP_TIMER  -> {
                    updateUI()
                }

                ACTION_UPDATE_DATA_PLAYLIST -> {
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(100)
                        updateFavourite()
                    }
                }
            }
        }

    }

    private lateinit var externalPlayer: MediaPlayerManager
    private var isPlayingMode = false // Track if the song is playing or paused

    private var previousSong = Audio.EMPTY_SONG

    private fun setUpPlayer() {
        binding.songName.isSelected = true
        binding.songAuthor.isSelected = true
        // Handle the incoming intent (if any)
        if (Intent.ACTION_VIEW == intent.action) {
            playSongOutside()
        } else {
            controlNormalSong()
        }

    }

    private fun checkShuffle(): Int {
        return if (MusicPlayerRemote.getShuffleMode() == MusicService.SHUFFLE_MODE_SHUFFLE) {
            R.drawable.icon_shuffle
        } else R.drawable.icon_no_shuffle
    }

    private suspend fun updateFavourite() {
        val isFavourite =
            PlaylistUtils.getInstance(this)?.checkIsFavourite(MusicPlayerRemote.getCurrentSong())
        println("updateFavourite: $isFavourite")
        val res = if (isFavourite == true) {
            R.drawable.icon_favourite
        } else {
            R.drawable.icon_unfavourite
        }
        withContext(Dispatchers.Main) {
            binding.playingFavourite.setImageResource(res)
        }
    }

    private val rotateAnimation: ObjectAnimator by lazy { createRotateAnimation(binding.playingSongAvatar) }

    private fun createRotateAnimation(playingSongIcon: ImageView): ObjectAnimator {
        return ObjectAnimator.ofFloat(playingSongIcon, "rotation", 0f, 360f).apply {
            duration = 10000 //Time for each completed round.
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.RESTART
            interpolator = LinearInterpolator()
            start()
        }

    }

    private fun updateImage() {
        rotateAnimation.cancel()
        binding.playingSongAvatar.rotation = 0f
        if (!rotateAnimation.isRunning) {
            rotateAnimation.start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scope = CoroutineScope(Dispatchers.Main)
        setUpPlayer()

        binding.backButton.setOnClickListener {
            if (Intent.ACTION_VIEW == intent.action) {
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(R.anim.fade_in_quick, R.anim.exit_to_top)
            } else {
                finish()
                overridePendingTransition(R.anim.fade_in_quick, R.anim.exit_to_top)
            }

        }

        if (Intent.ACTION_VIEW != intent.action) {
            scope.launch {
                while (isActive) {
                    val progress = MusicPlayerRemote.getSongProgressMillis()
                    val max = MusicPlayerRemote.getSongDurationMillis()
                    binding.currentTime.text =
                       Utils.formatDuration(progress)
                    binding.totalTime.text =
                       Utils.formatDuration(max.toLong())
                    binding.playingSeekbar.progress =
                        MusicPlayerRemote.getSongProgressMillis().toInt()
                    delay(100)
                }
            }

            binding.morePlayer.setOnClickListener {
                println("PlayerActivity")
                val dialog = PlayingBottomSheet(this@PlayerActivity)
                val current = MusicPlayerRemote.getCurrentSong()
                dialog.setPlayingAudio(current)
                dialog.show()
            }
        }

        binding.root.setOnSWipeListener(this)

        binding.playingSeekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                MusicPlayerRemote.seekTo(seekBar?.progress ?: 0)
            }

        })

        binding.playingPower.setOnClickListener {
            if (MusicPlayerRemote.isPlaying()) {
                MusicPlayerRemote.pauseSong()
            } else {
                MusicPlayerRemote.resumePlaying()
            }
        }

        binding.playingNext.setOnClickListener {
            MusicPlayerRemote.playNextSong()
        }

        binding.playingPrevious.setOnClickListener {
            MusicPlayerRemote.playPreviousSong()
        }
        binding.playingAddPlaylist.setOnClickListener {
            val addPlaylistBottomSheet = AddPlaylistBottomSheet(this)
            addPlaylistBottomSheet.setData(MusicPlayerRemote.getCurrentSong())
            addPlaylistBottomSheet.show()
        }

        binding.playingShare.setOnClickListener {
//            val currentSong = MusicPlayerRemote.getCurrentSong()
//            Utils.shareVideoOrAudio(
//                this@PlayerActivity,
//                currentSong.mediaObject?.title,
//                currentSong.mediaObject?.path
//            )
        }

    }

    private fun setupBackgroundForPlayerScreen(bitmap: Bitmap?) {
        println("setupBackgroundForPlayerScreen: ${bitmap == null}")
        if(bitmap == null) {
            return
        }
        val blurred = blurBitmap(this, bitmap, 20f)

        val drawable = BitmapDrawable(resources, blurred).apply {
            alpha = (255 * 0.2).toInt() // 20% transparency
        }

       binding.playerBackground.background = drawable
    }

    private fun playSongOutside() {
        val data = intent.data
        if (data != null) {
            binding.bonusFunction.visibility = View.GONE
            val bitmap = Utils.getBitmapFromUri(data, this)
            bitmap?.let {
                Glide.with(this).load(it).into(binding.playingSongAvatar)
                setupBackgroundForPlayerScreen(it)
            }

            Glide.with(this).load(bitmap).placeholder(R.drawable.app_logo)
                .error(R.drawable.app_logo).into(
                    binding.playingSongAvatar
                )

            binding.playingQueue.setImageResource(R.drawable.icon_open_queue)
            binding.playingShuffle.setImageResource(R.drawable.icon_no_shuffle)
            binding.playingPower.setImageResource(R.drawable.icon_pause)

            val info = Utils.getMP3Metadata(data, this)
            binding.songName.text = info.first
            binding.songAuthor.text = info.second

            // Example: Using MediaPlayer to play the MP3 file
            externalPlayer = MediaPlayerManager(this,
                onPrepared = { duration ->
                    updateImage()
                    binding.totalTime.text = Utils.formatDuration(duration.toLong())
                    binding.playingSeekbar.max = duration
                    updateSeekbar()
                },
                onCompletion = {
                    stopExternalPlayback()
                }
            )
            externalPlayer.load(data)
            isPlayingMode = true
            binding.playingPower.setImageResource(R.drawable.icon_pause)

            binding.playingPower.setOnClickListener {
                if (isPlayingMode) {
                    externalPlayer.pause()
                    isPlayingMode = false
                    rotateAnimation.pause()
                    binding.playingPower.setImageResource(R.drawable.icon_play)
                } else {
                    // Pause the song
                    externalPlayer.play()
                    isPlayingMode = true
                    rotateAnimation.resume()
                    updateSeekbar()
                    binding.playingPower.setImageResource(R.drawable.icon_pause)
                }
            }

            // Listen for SeekBar changes (user interaction)
            binding.playingSeekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        // If the user manually changes the SeekBar, update the media player position
                        externalPlayer.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    // Optionally handle user starting to drag the SeekBar
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    // Optionally handle user stopping to drag the SeekBar
                }
            })



        }
    }

    private fun stopExternalPlayback() {
        externalPlayer.pause()
        rotateAnimation.pause()
        binding.playingPower.setImageResource(R.drawable.icon_play)
        isPlayingMode = false
    }

    private fun updateSeekbar() {
        lifecycleScope.launch {
            try {
                while (true) {
                    try {
                        if (externalPlayer.isPlaying()) {
                            val currentPosition = externalPlayer.getCurrentPosition()
                            binding.playingSeekbar.progress = currentPosition
                            binding.currentTime.text =
                                Utils.formatDuration(
                                    currentPosition.toLong()
                                )

                        } else {
                            break
                        }
                    } catch (e: IllegalStateException) {
                        Log.e("Seekbar", "MediaPlayer is in an illegal state", e)
                        break // Exit loop if MediaPlayer is in a bad state

                    }
                }
                delay(1000)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun controlNormalSong() {
        val currentAudio = MusicPlayerRemote.getCurrentSong()
        MusicPlayerRemote.setCurrentAudio(currentAudio)
        binding.bonusFunction.visibility = View.VISIBLE

        val songArt =
         Utils.getAlbumArt(currentAudio.mediaObject?.path ?: "")
        val backgroundBitmap = Utils.getAlbumArt(currentAudio.mediaObject?.path ?: "")
        println("currentAudio: $songArt")

        val currentSong = MusicPlayerRemote.getCurrentSong()

        if(previousSong != currentSong) {
            previousSong = currentSong

            Glide.with(this).load(songArt)
                .placeholder(R.drawable.app_logo).error(R.drawable.app_logo)
                .into(binding.playingSongAvatar)
            setupBackgroundForPlayerScreen(backgroundBitmap)
        }


        binding.songName.text = currentAudio.mediaObject?.title ?: ""
        binding.songAuthor.text =
            if (!currentAudio.artistName[0].isLetter() || currentAudio.artistName.contains(
                    Constant.UNKNOWN_STRING,
                    true
                )
            ) this.resources.getString(R.string.unknown_artist) else currentAudio.artistName


        val res = if (MusicPlayerRemote.isPlaying()) {
            rotateAnimation.resume()
            R.drawable.icon_pause
        } else {
            rotateAnimation.pause()
            R.drawable.icon_play
        }


        binding.playingFavourite.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val isFavourite = PlaylistUtils.getInstance(this@PlayerActivity)
                    ?.checkIsFavourite(MusicPlayerRemote.getCurrentSong())
                if (isFavourite == true) {
                    PlaylistUtils.getInstance(this@PlayerActivity)
                        ?.removeFromFavourite(MusicPlayerRemote.getCurrentSong())
                } else {
                    PlaylistUtils.getInstance(this@PlayerActivity)?.addToFavourite(
                        MusicPlayerRemote.getCurrentSong()
                    )
                }
                updateFavourite()
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            updateFavourite()
        }

//        val resRepeat = when (MusicPlayerRemote.getRepeatMode()) {
//            MusicService.REPEAT_MODE_ALL -> R.drawable.icon_all_duplicate
//            MusicService.REPEAT_MODE_THIS -> R.drawable.icon_one_duplicate
//            else -> R.drawable.icon_no_duplicate
//        }
//
//        binding.playingShuffle.setImageResource(resRepeat)
//
//        binding.playingShuffle.setOnClickListener {
//            MusicPlayerRemote.cycleRepeatMode()
//            val resourceRepeat = when (MusicPlayerRemote.getRepeatMode()) {
//                MusicService.REPEAT_MODE_ALL -> R.drawable.icon_all_duplicate
//                MusicService.REPEAT_MODE_THIS -> R.drawable.icon_one_duplicate
//                else -> R.drawable.icon_no_duplicate
//            }
//
//            binding.playingShuffle.setImageResource(resourceRepeat)
//        }

        val resShuffle = checkShuffle()

        binding.playingShuffle.setImageResource(resShuffle)

        binding.playingShuffle.setOnClickListener {
            MusicPlayerRemote.toggleShuffleMode()
            val resourceShuffle = checkShuffle()

            binding.playingShuffle.setImageResource(resourceShuffle)
        }

        binding.playingSleepTimer.setOnClickListener {
            println("playingSleepTimer: here")
            val dialog = TimeSleepBottomSheet(context = this@PlayerActivity)
            dialog.show()
        }

        binding.playingPower.setImageResource(res)

        binding.playingSeekbar.max = MusicPlayerRemote.getSongDurationMillis()
        binding.playingSeekbar.progress = MusicPlayerRemote.getSongProgressMillis().toInt()

        binding.playingQueue.setOnClickListener {
            startActivity(Intent(this@PlayerActivity, PlayingQueueActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        intentFilter.addAction(
            MusicService.PLAY_STATE_CHANGED
        )
        intentFilter.addAction(
            MusicService.META_CHANGED
        )
        intentFilter.addAction(
            MusicService.NEW_SONG_CHANGED
        )
        intentFilter.addAction(
            ACTION_UPDATE_DATA_PLAYLIST
        )
        intentFilter.addAction(
           MusicService.ACTION_UPDATE_SLEEP_TIMER
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(broadcastPlayerMusic, intentFilter, Context.RECEIVER_EXPORTED)
        } else {
            ContextCompat.registerReceiver(
                this,
                broadcastPlayerMusic,
                intentFilter,
                ContextCompat.RECEIVER_EXPORTED
            )

        }
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(broadcastPlayerMusic)
        } catch (e: IllegalArgumentException) {
            Log.w("PlayerActivity", "Receiver not registered", e)
        }

        if (::externalPlayer.isInitialized) {
            externalPlayer.release()
        }
    }

    private fun updateUI() {
        binding.apply {
            if (MusicPlayerRemote.timeLeft() > 0) {
                playingSleepTimer.setOnClickListener {
                    val dialog = CountDownBottomSheet(context = this@PlayerActivity)
                    dialog.show()
                }

                println("updateUI: ${MusicPlayerRemote.timeLeft()}")
                playingIcon.setImageResource(R.drawable.icon_countdown)
                playingTitle.visible()
                playingTitle.text = com.example.service.utils.Utils.getDuration(MusicPlayerRemote.timeLeft())
            } else {
                playingSleepTimer.setOnClickListener {
                    val dialog = TimeSleepBottomSheet(context = this@PlayerActivity)
                    dialog.show()
                }
                playingIcon.setImageResource(R.drawable.icon_no_countdown)
                playingTitle.gone()
            }
        }
    }

    override fun onBackPressed() {
        if (Intent.ACTION_VIEW == intent.action) {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(R.anim.fade_in_quick, R.anim.exit_to_top)
        } else {
            finish()
            overridePendingTransition(R.anim.fade_in_quick, R.anim.exit_to_top)
        }
    }

    override fun onResume() {
        super.onResume()
        checkShuffle()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::externalPlayer.isInitialized) {
            externalPlayer.release()
        }
    }

    companion object {
        const val PLAY_SPEED = "PLAY_SPEED"
    }
}