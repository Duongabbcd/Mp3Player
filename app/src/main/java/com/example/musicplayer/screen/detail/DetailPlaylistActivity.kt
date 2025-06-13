package com.example.musicplayer.screen.detail

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.base.BaseActivity
import com.example.musicplayer.databinding.ActivityDetailPlaylistBinding
import com.example.musicplayer.screen.detail.bottomsheet.EditPlaylistBottomSheet
import com.example.musicplayer.screen.detail.bottomsheet.PlaylistInfoBottomSheet
import com.example.musicplayer.screen.home.MainActivity
import com.example.musicplayer.screen.home.adapter.SongAdapter
import com.example.musicplayer.utils.Common.gone
import com.example.musicplayer.utils.Common.visible
import com.example.musicplayer.viewmodel.MainViewModel
import com.example.service.model.Audio
import com.example.service.service.MusicPlayerRemote
import com.example.service.service.MusicService
import com.example.service.service.MusicService.Companion.SHUFFLE_MODE_SHUFFLE
import com.example.service.utils.PlaylistUtils
import com.example.service.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailPlaylistActivity :
    BaseActivity<ActivityDetailPlaylistBinding>(ActivityDetailPlaylistBinding::inflate) {
    private val mainViewModel: MainViewModel by viewModels()

    private var isShowed =  false
    private val songAdapter by lazy {
        SongAdapter(true) {
            //do nothing
        }
    }

    private val broadcastChange = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action == Utils.ACTION_FINISH_DOWNLOAD) {
                mainViewModel.getPlaylist(idPlaylist)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isShowed = intent.getBooleanExtra("Photo", false)
        binding.apply {
            backBtn.setOnClickListener {
                startActivity(Intent(this@DetailPlaylistActivity, MainActivity::class.java))
                finish()
            }
            allSongs.adapter = songAdapter

            mainViewModel.getPlaylist(idPlaylist)

            mainViewModel.currentPlaylist.observe(this@DetailPlaylistActivity) { item ->
                if (item.id == -1) {
                    binding.moreIcon.gone()
                }

                if (isShowed) {
                    val dialog =
                        EditPlaylistBottomSheet(this@DetailPlaylistActivity) { result, image, title ->
                            if (result) {
                                isShowed = false
                                CoroutineScope(Dispatchers.IO).launch {
                                    PlaylistUtils.getInstance(this@DetailPlaylistActivity)
                                        ?.updatePlaylistInfo(item, image, title)
                                }
                                Glide.with(this@DetailPlaylistActivity).load(image)
                                    .placeholder(R.drawable.icon_avt_song).error(R.drawable.icon_avt_song)
                                    .into(binding.playlistAvatar)
                                playlistName.text = title
                            }
                        }
                    dialog.updatePlaylist(playlist = item)
                    dialog.show()
                }

                moreIcon.setOnClickListener {
                    val dialog =
                        PlaylistInfoBottomSheet(this@DetailPlaylistActivity) { result, image, title ->
                            if (result) {
                                isShowed = false
                                CoroutineScope(Dispatchers.IO).launch {
                                    PlaylistUtils.getInstance(this@DetailPlaylistActivity)
                                        ?.updatePlaylistInfo(item, image, title)
                                }

                                Glide.with(this@DetailPlaylistActivity).load(image)
                                    .placeholder(R.drawable.icon_avt_song).error(R.drawable.icon_avt_song)
                                    .into(binding.playlistAvatar)
                                playlistName.text = title
                            }
                        }
                    dialog.updatePlaylist(playlist = item)
                    dialog.show()
                }

                Glide.with(this@DetailPlaylistActivity).load(Uri.parse(item.thumbUri))
                    .placeholder(R.drawable.icon_avt_song).error(R.drawable.icon_avt_song)
                    .into(binding.playlistAvatar)

                playlistName.text = item.title

                val size = item.tracks.size
                val title = if (size <= 1) {
                    resources.getString(R.string.one_song)
                } else {
                    resources.getString(R.string.many_songs)
                }
                playlistSongCount.text = "($size) $title"

                if (size == 0) {
                    allSongs.gone()
                    controllerLayout.gone()
                    emptyTitle.visible()
                    return@observe
                } else {
                    allSongs.visible()
                    controllerLayout.visible()
                    emptyTitle.gone()
                }
            }

            mainViewModel.allPlaylistSong.observe(this@DetailPlaylistActivity) { items ->
                songAdapter.updateData(items)
                shuffleBtn.setOnClickListener {
                    MusicPlayerRemote.openAndShuffleQueue(ArrayList(items), true)
                    MusicPlayerRemote.toggleShuffleMode(SHUFFLE_MODE_SHUFFLE)
                    val result = MusicPlayerRemote.getPlayingQueue() as ArrayList<Audio>
                    mainViewModel.getAllSongs(result)
                }

                playAllBtn.setOnClickListener {
                    MusicPlayerRemote.openQueue(ArrayList(items), 0, true)
                }
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

    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(broadcastChange)
    }


    override fun onBackPressed() {
        startActivity(Intent(this@DetailPlaylistActivity, MainActivity::class.java))
        finish()
    }

    companion object {
        var idPlaylist = -1
    }
}