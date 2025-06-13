package com.example.musicplayer.screen.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.base.BaseActivity
import com.example.musicplayer.databinding.ActivityPlayingArtistBinding
import com.example.musicplayer.screen.home.adapter.SongAdapter
import com.example.musicplayer.screen.home.bottomsheet.SortBottomSheet
import com.example.musicplayer.utils.Common.gone
import com.example.musicplayer.utils.Common.visible
import com.example.musicplayer.utils.Constant
import com.example.musicplayer.viewmodel.MainViewModel
import com.example.service.model.Artist
import com.example.service.model.Audio
import com.example.service.service.MusicPlayerRemote
import com.example.service.service.MusicService
import com.example.service.service.MusicService.Companion.SHUFFLE_MODE_SHUFFLE
import com.example.service.utils.PreferenceUtil
import com.example.service.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class PlayingArtistActivity :
    BaseActivity<ActivityPlayingArtistBinding>(ActivityPlayingArtistBinding::inflate) {

    private val mainViewModel: MainViewModel by viewModels()
    private val allSongAdapter = SongAdapter { item -> mainViewModel.getSelectedAudios(item) }
    private val listData = mutableListOf<Audio>()

    private val idArtist by lazy {
        artist.nameArtist
    }

    private val broadcastChange = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
//            if (action == MusicService.PLAY_STATE_CHANGED.replace(
//                    MusicService.MUSIC_PLAYER_PACKAGE_NAME, MusicService.MUSIC_PACKAGE_NAME
//                )
//            ) {
//                allSongAdapter.notifyDataSetChanged()
//            }
            if (action == Utils.ACTION_FINISH_DOWNLOAD) {
                reloadData()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        intentFilter.addAction(
            MusicService.PLAY_STATE_CHANGED.replace(
                MusicService.MUSIC_PLAYER_PACKAGE_NAME, MusicService.MUSIC_PACKAGE_NAME
            )
        )
        intentFilter.addAction(Utils.ACTION_FINISH_DOWNLOAD)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                broadcastChange, intentFilter, Context.RECEIVER_EXPORTED
            )
        } else {
            ContextCompat.registerReceiver(
                this,
                broadcastChange,
                intentFilter,
                ContextCompat.RECEIVER_EXPORTED
            )

        }
        allSongAdapter.notifyItemRangeChanged(0, listData.size)

        setupPlayingSongController()
    }

    private fun setupPlayingSongController() {
        artistShuffleMode = 0
    }

    private fun reloadData() {
        try {
            println("idArtist: $idArtist")
            mainViewModel.getAllArtistSong(idArtist)

            mainViewModel.allArtistSong.observe(this) { items ->
                println("items: $items")
                allSongAdapter.updateData(items)

                listData.addAll(items)
                binding.shuffleBtn.setOnClickListener {
                    MusicPlayerRemote.openAndShuffleQueue(ArrayList(items), true)
                    MusicPlayerRemote.toggleShuffleMode(SHUFFLE_MODE_SHUFFLE)
                    val result = MusicPlayerRemote.getPlayingQueue() as ArrayList<Audio>
                    mainViewModel.getAllSongs(result)
                }

                binding.playAllBtn.setOnClickListener {
                    MusicPlayerRemote.openQueue(ArrayList(items), 0, true)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.artistDetailRV.adapter = allSongAdapter
        binding.apply {
            artistFullName.isSelected  = true
            numberSong.isSelected  = true
            reloadData()
            val quantityString = if (artist.numberOfAlbum.isEmpty()) {
                "0 ".plus(this@PlayingArtistActivity.getString(R.string.one_song))
            } else {
                if (artist.numberOfAlbum.toInt() in listOf(
                        0, 1
                    )
                ) this@PlayingArtistActivity.getString(R.string.one_song) else
                    this@PlayingArtistActivity.getString(R.string.many_songs)
            }
            val firstArtistName = artist.nameArtist[0]
            val artistName = if (!firstArtistName.isLetter() || artist.nameArtist.contains(
                    Constant.UNKNOWN_STRING,
                    true
                )
            ) this@PlayingArtistActivity.resources.getString(R.string.unknown_artist) else artist.nameArtist
            println("On create: ${artist.nameArtist} and $artistName")

            numberSong.text = if (artist.numberSong.isEmpty()) {
                quantityString
            } else artist.numberSong.plus(" $quantityString")
            artistFullName.text = artistName
            artistFullName.isSelected = true

            backBtn.setOnClickListener {
                finish()
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }

            appBarLayout.addOnOffsetChangedListener { _, verticalOffset ->
                println("verticalOffset: $verticalOffset and ${appBarLayout.totalScrollRange}")
                val isExpand = abs(verticalOffset) < appBarLayout.totalScrollRange
                expandLayout.isVisible = isExpand

                if (isExpand) {
                    artistFullName.text = artistName
                } else {
                    artistFullName.text = artistName
                    toolbar.setBackgroundColor(Color.TRANSPARENT)
                }
            }


            binding.scrollToTop.setOnClickListener {
                binding.artistDetailRV.smoothScrollToPosition(0)
            }

            artistDetailRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                    allSongAdapter.lastVisibleItemPosition = lastVisibleItemPosition

                    scrollToTop.isVisible = firstVisibleItemPosition > 0
                }
            })

            filter.setOnClickListener {

                val dialog = SortBottomSheet(this@PlayingArtistActivity, false) { string ->
                    PreferenceUtil.getInstance(this@PlayingArtistActivity)?.setSongSortOrder(string)
                    mainViewModel.getAllArtistSong(idArtist)
                }
                dialog.show()

            }

            mainViewModel.isShowSelection.observe(this@PlayingArtistActivity) { input ->
                println("displaySelection 1: $input")
                allSongAdapter.displaySelection(input)
                if (input) {
                    deleteLayout.visible()
                } else {
                    deleteLayout.gone()
                }
            }


        }
    }

    override fun setUpMiniPlayer() {
        super.setUpMiniPlayer()
        allSongAdapter.notifyItemRangeChanged(0, listData.size)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(broadcastChange)
    }

    override fun onResume() {
        super.onResume()
        allSongAdapter.notifyItemRangeChanged(0, listData.size)
    }


    companion object {
        var artist = Artist()
        var artistShuffleMode = 0
    }
}