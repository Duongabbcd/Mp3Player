package com.example.musicplayer.screen.home.dialog

import android.app.Dialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import com.example.musicplayer.R
import com.example.musicplayer.base.BaseActivity
import com.example.musicplayer.databinding.DialogDeleteBinding
import com.example.service.model.Audio
import com.example.service.utils.MusicUtil
import com.example.service.utils.PlaylistUtils
import com.example.service.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeleteSongDialog(
    context: Context
) : Dialog(context) {
    private val binding by lazy { DialogDeleteBinding.inflate(layoutInflater) }

    init {
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    fun deleteAudioFromDevice(data: Any) {
        println("deleteAudioFromDevice: $data")
        val audio = data as Audio
        val title = audio.mediaObject?.title ?: ""
        binding.apply {
            deleteQuestion.text = context.resources.getString(R.string.delete_question, title)
            textCancel.setOnClickListener {
                dismiss()
            }

            textOk.setOnClickListener {
                val intent = Intent(BaseActivity.ACTION_DELETE_FILE)
                intent.putExtra(BaseActivity.KEY_ID_SONG, audio.mediaObject?.path)
                context.sendBroadcast(intent)
                dismiss()
            }
        }
    }

    fun deleteManyAudiosFromDevice(
        audios: List<Audio>,
        onResult: (Boolean) -> Unit
    ) {
        val filePaths = audios.map { it.mediaObject?.id ?: "" }
        val uris = mutableListOf<Uri>()
        val audioIds = mutableListOf<Int>()
        for(path in filePaths) {
            val id = path.toIntOrNull() ?: return
            audioIds.add(id)
            uris.add(MusicUtil.getSongFileUri(id))
        }

        binding.apply {
            val totalSize = audios.size
            val title = if (totalSize <= 1) {
                context.resources.getString(R.string.delete_question_2, audios.size.toString())
            } else {
                context.resources.getString(R.string.delete_question_3, audios.size.toString())
            }
            deleteQuestion.text = context.resources.getString(R.string.delete_question, title)
            textOk.setOnClickListener {
                onResult(true)
                dismiss()
            }

            textCancel.setOnClickListener {
                onResult(false)
                dismiss()
            }
        }
    }

    private fun deleteAudioFile(context: Context, filePath: String): Boolean {
        try {
            val contentResolver = context.contentResolver

            val projection = arrayOf(MediaStore.Audio.Media._ID)
            val selection = "${MediaStore.Audio.Media.DATA} = ?"
            val selectionArgs = arrayOf(filePath)

            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val id = cursor.getLong(idColumn)
                    val contentUri = ContentUris.withAppendedId(uri, id)

                    val rowsDeleted = contentResolver.delete(contentUri, null, null)
                    return rowsDeleted > 0
                }
            }

            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

}



enum class DeleteMode {
    ARTIST,
    ALBUM,
    QUEUE,
    PLAYLIST,
    AUDIO,
    PLAYING_SONG,
    DEFAULT
}