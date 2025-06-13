package com.example.musicplayer.screen.detail.bottomsheet

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.lifecycleScope
import com.example.musicplayer.R
import com.example.musicplayer.base.BaseBottomSheetDialog
import com.example.musicplayer.databinding.BottomSheetDeleteSongBinding
import com.example.musicplayer.databinding.BottomSheetEditPlaylistBinding
import com.example.service.model.Audio
import com.example.service.model.Playlist
import com.example.service.utils.PlaylistUtils
import com.example.service.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeleteSongBottomSheet (private val context: Context) :
    BaseBottomSheetDialog<BottomSheetDeleteSongBinding>(context) {
    override fun getViewBinding(): BottomSheetDeleteSongBinding {
        return BottomSheetDeleteSongBinding.inflate(layoutInflater)
    }

    // Define a coroutine scope tied to this dialog
    private val dialogScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)


    override fun initViews() {
        setContentView(binding.root)
    }

    fun deleteAudioFromPlaylist(song: Audio, idPlaylist: Int) {
        println("deleteAudioFromPlaylist: $song")
        val title = song.mediaObject?.title.orEmpty()
        binding.apply {
            deleteQuestion.text = context.getString(R.string.delete_question, title)
            createBtn.setOnClickListener {
                dialogScope.launch(Dispatchers.IO) {
                    val currentPlaylist = PlaylistUtils.getInstance(context)
                        ?.getPlaylistById(idPlaylist) ?: return@launch

                    val index = currentPlaylist.tracks.indexOf(song)
                    if (index != -1) {
                        currentPlaylist.tracks.removeAt(index)
                    }

                    PlaylistUtils.getInstance(context)?.savePlaylist(currentPlaylist)
                    context.sendBroadcast(Intent(Utils.ACTION_FINISH_DOWNLOAD))

                    withContext(Dispatchers.Main) {

                        dismiss()
                    }
                }
            }

            cancelBtn.setOnClickListener {
                dismiss()
            }
        }

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        dialogScope.cancel() // Clean up coroutines when dialog is dismissed
    }

}