package com.example.musicplayer.screen.detail.bottomsheet

import android.content.Context
import android.net.Uri
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.base.BaseBottomSheetDialog
import com.example.musicplayer.databinding.BottomSheetEditPlaylistBinding
import com.example.musicplayer.databinding.BottomSheetPlaylistInfoBinding
import com.example.service.model.Playlist
import com.example.service.utils.PlaylistUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlaylistInfoBottomSheet(private val context: Context,  private val onUpdateInfoListener: (Boolean, Uri, String) -> Unit) :
    BaseBottomSheetDialog<BottomSheetPlaylistInfoBinding>(context) {
    private var currentPlaylist: Playlist? = null

    fun updatePlaylist(playlist: Playlist) {
        println("updatePlaylist: $playlist")
        currentPlaylist = playlist
        binding.apply {
            val size = playlist.tracks.size
            val title = if(size <= 1) context.getString(R.string.one_song) else context.getString(R.string.many_songs)
            playlistName.text = playlist.title
            playlistSize.text = "$size $title"

            Glide.with(context).load(playlist.thumbUri).placeholder(R.drawable.icon_avt_song).error(R.drawable.icon_avt_song).into(playlistAvatar)

            editPlaylist.setOnClickListener {
                val dialog =
                    EditPlaylistBottomSheet(context) { result, image, title ->
                        if (result) {
                            onUpdateInfoListener(result, image, title)
                            playlistName.text = title
                        }
                    }
                dialog.updatePlaylist(playlist = playlist)
                dialog.show()
                dismiss()
            }
        }
    }

    override fun getViewBinding(): BottomSheetPlaylistInfoBinding {
        return BottomSheetPlaylistInfoBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        setContentView(binding.root)
    }

}