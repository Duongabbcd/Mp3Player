package com.example.musicplayer.screen.artist

import android.annotation.SuppressLint
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
import com.example.musicplayer.databinding.ActivityArtistBinding
import com.example.musicplayer.screen.home.adapter.SongAdapter
import com.example.musicplayer.utils.Common.gone
import com.example.musicplayer.utils.Common.visible
import com.example.musicplayer.utils.Constant
import com.example.musicplayer.viewmodel.MainViewModel
import com.example.service.model.Artist
import com.example.service.model.Audio
import com.example.service.service.MusicPlayerRemote
import com.example.service.service.MusicService
import com.example.service.service.MusicService.Companion.SHUFFLE_MODE_NONE
import com.example.service.service.MusicService.Companion.SHUFFLE_MODE_SHUFFLE
import com.example.service.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs


@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class ArtistActivity : BaseActivity<ActivityArtistBinding>(ActivityArtistBinding::inflate) {


    private val mainViewModel: MainViewModel by viewModels()
    private val allSongAdapter = SongAdapter  {   item ->      mainViewModel.getSelectedAudios(item) }
    private val listData = mutableListOf<Audio>()


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

        binding.shuffleBtn.setOnClickListener {
            artistShuffleMode = 1
            MusicPlayerRemote.openAndShuffleQueue(ArrayList(listData),true)
            checkShuffle()
            val x: ArrayList<Any> = MusicPlayerRemote.getPlayingQueue()
            listData.clear()
            listData.addAll(x as ArrayList<Audio>)
            allSongAdapter.updateData(x)
        }

        setupPlayingSongController()
    }

    private fun setupPlayingSongController() {
        artistShuffleMode = 0
    }

    private fun checkShuffle() {
        val resource = if(artistShuffleMode == SHUFFLE_MODE_NONE)
            R.drawable.icon_no_shuffle
        else R.drawable.icon_shuffle
        binding.shuffleIcon.setImageResource(resource).also {
            sendBroadcast(
                Intent(
                    MusicService.PLAY_STATE_CHANGED.replace(
                        MusicService.MUSIC_PLAYER_PACKAGE_NAME, MusicService.MUSIC_PACKAGE_NAME
                    )) )
        }
    }

    private fun reloadData() {
        try {
            val artistId =  artist.nameArtist
            mainViewModel.getAllArtistSong( artistName = artistId)


            mainViewModel.allAlbumSong.observe(this) { items ->
                items.onEach {
                    println("reloadData $it")
                }
                allSongAdapter.updateData(items)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding.artistDetailRV.adapter = allSongAdapter
        binding.artistDetailRV.layoutManager = LinearLayoutManager(this)

        binding.apply {
            reloadData()
            val quantityString = if (artist.numberOfAlbum.isEmpty()) {
                "0 ".plus(this@ArtistActivity.getString(R.string.one_song))
            } else {
                if (artist.numberOfAlbum.toInt() in listOf(
                        0, 1
                    )
                ) this@ArtistActivity.getString(R.string.one_song) else
                    this@ArtistActivity.getString(R.string.many_songs)
            }
            val firstArtistName = artist.nameArtist[0]
            val artistName = if (!firstArtistName.isLetter() || artist.nameArtist.contains(
                    Constant.UNKNOWN_STRING,
                    true
                )
            ) this@ArtistActivity.resources.getString(R.string.unknown_artist) else artist.nameArtist
            println("On create: ${artist.nameArtist}")

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
                val isExpand = abs(verticalOffset) != appBarLayout.totalScrollRange
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

            select.setOnClickListener {
                mainViewModel.checkSelection(true)
            }

            mainViewModel.isShowSelection.observe(this@ArtistActivity) { input ->
                println("displaySelection 1: $input")
                allSongAdapter.displaySelection(input)
                if (input) {
                    deleteLayout.visible()
                } else {
                    deleteLayout.gone()
                }
            }

            mainViewModel.allArtistSong.observe(this@ArtistActivity) { items ->
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

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }


    companion object {
        var artist = Artist()
        var artistShuffleMode = 0
    }
}