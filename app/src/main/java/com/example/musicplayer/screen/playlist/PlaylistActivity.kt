package com.example.musicplayer.screen.playlist

import com.example.musicplayer.base.BaseActivity
import com.example.musicplayer.databinding.ActivityPlaylistBinding

class PlaylistActivity  : BaseActivity<ActivityPlaylistBinding>(ActivityPlaylistBinding::inflate){

    companion object {
        const val ACTION_UPDATE_DATA_PLAYLIST = "ACTION_UPDATE_DATA_PLAYLIST"
    }
}