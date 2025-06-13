package com.example.musicplayer.screen.playing_queue

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.base.BaseActivity
import com.example.musicplayer.databinding.ActivityPlayingQueueBinding
import com.example.musicplayer.screen.playing_queue.adapter.PlayingQueueAdapter
import com.example.musicplayer.utils.Utils.setOnSWipeListener
import com.example.service.model.Audio
import com.example.service.service.MusicPlayerRemote
import com.example.service.service.MusicService
import com.example.service.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayingQueueActivity  : BaseActivity<ActivityPlayingQueueBinding>(ActivityPlayingQueueBinding::inflate){
    private val playingAdapter = PlayingQueueAdapter()
    private val listDataSong = mutableListOf<Any>()



    private val broadcastChange = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val action = intent.action
            println("onReceive: $action")
            if (action == MusicService.PLAY_STATE_CHANGED.replace(
                    MusicService.MUSIC_PLAYER_PACKAGE_NAME, MusicService.MUSIC_PACKAGE_NAME
                ) || action == Utils.ACTION_FINISH_DOWNLOAD
            ) {
                reloadData()
                playingAdapter.notifyDataSetChanged()
            }
        }
    }


    private fun reloadData() {
        CoroutineScope(Dispatchers.IO).launch {
            val listSong = MusicPlayerRemote.getPlayingQueue()
            listDataSong.clear()
            listDataSong.addAll(listSong)

            binding.playlistTitle.text = "(${listSong.size}) song"

            if (listSong.isEmpty()) {
                return@launch
            }

            withContext(Dispatchers.Main) {
                binding.allSongs.adapter = playingAdapter
                playingAdapter.updateData(listDataSong as List<Audio>)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scope = CoroutineScope(Dispatchers.Main)
        if (Intent.ACTION_VIEW != intent.action) {
            scope.launch {
                while (isActive) {
                    val progress = MusicPlayerRemote.getSongProgressMillis()
                    val max = MusicPlayerRemote.getSongDurationMillis()
                    binding.currentTime.text =
                        com.example.musicplayer.utils.Utils.formatDuration(progress)
                    binding.totalTime.text =
                        com.example.musicplayer.utils.Utils.formatDuration(max.toLong())
                    binding.playingSeekbar.progress =
                        MusicPlayerRemote.getSongProgressMillis().toInt()
                    delay(100)
                }
            }
        }

        binding.backBtn.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        reloadData()

        val currentSong = MusicPlayerRemote.getCurrentSong()
        val itemTouchHelper = ItemTouchHelper(DragManageAdapter(playingAdapter))
        itemTouchHelper.attachToRecyclerView(binding.allSongs)

        val layoutManager = LinearLayoutManager(this)
        val currentSongIndex =
            MusicPlayerRemote.getPlayingQueue().indexOf(currentSong)
        layoutManager.scrollToPositionWithOffset(currentSongIndex, 0)

        binding.allSongs.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val customLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition = customLayoutManager.findFirstVisibleItemPosition()
                val lastVisibleItemPosition = customLayoutManager.findLastVisibleItemPosition()
                playingAdapter.lastVisibleItemPosition = lastVisibleItemPosition

                binding.scrollToTop.isVisible =
                    firstVisibleItemPosition > 0 && recyclerView.scrollState == RecyclerView.SCROLL_STATE_DRAGGING
            }
        })

        binding.scrollToTop.setOnClickListener {
            binding.allSongs.smoothScrollToPosition(0)
        }

        binding.root.setOnSWipeListener(this)
        binding.root.setOnClickListener {

        }

        binding.playingSeekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                MusicPlayerRemote.seekTo(seekBar?.progress ?: 0)
            }

        })
        controlNormalSong()
        binding.playingPower.setOnClickListener {
            if (MusicPlayerRemote.isPlaying()) {
                MusicPlayerRemote.pauseSong()
                binding.playingPower.setImageResource(R.drawable.icon_play)
            } else {
                MusicPlayerRemote.resumePlaying()
                binding.playingPower.setImageResource(R.drawable.icon_pause)
            }
        }

        binding.playingNext.setOnClickListener {
            MusicPlayerRemote.playNextSong()
        }

        binding.playingPrevious.setOnClickListener {
            MusicPlayerRemote.playPreviousSong()
        }
    }

    private fun controlNormalSong() {
        val currentAudio = MusicPlayerRemote.getCurrentSong()
        MusicPlayerRemote.setCurrentAudio(currentAudio)


        val res = if (MusicPlayerRemote.isPlaying()) {
            R.drawable.icon_pause
        } else {
            R.drawable.icon_play
        }

        binding.playingPower.setImageResource(res)

        binding.playingSeekbar.max = MusicPlayerRemote.getSongDurationMillis()
        binding.playingSeekbar.progress = MusicPlayerRemote.getSongProgressMillis().toInt()

    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    inner class DragManageAdapter(
        private val adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    ) : ItemTouchHelper.Callback() {
        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            //Allow drag upward or downward
            val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
            return makeMovementFlags(dragFlags, 0)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition
            println("fromPosition: $fromPosition and $toPosition" )
            adapter.notifyItemMoved(fromPosition, toPosition)
            MusicPlayerRemote.moveSong(fromPosition, toPosition)
            return true
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            super.clearView(recyclerView, viewHolder)
            try {
                if (!recyclerView.isComputingLayout && !recyclerView.isAnimating) {
                    playingAdapter.updateData(MusicPlayerRemote.getPlayingQueue())
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

        }
    }
}