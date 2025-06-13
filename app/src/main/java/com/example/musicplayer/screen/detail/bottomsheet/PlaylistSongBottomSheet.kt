package com.example.musicplayer.screen.detail.bottomsheet

import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import com.example.musicplayer.R
import com.example.musicplayer.base.BaseBottomSheetDialog
import com.example.musicplayer.databinding.BottomSheetPlaylistMoreBinding
import com.example.musicplayer.screen.home.dialog.DeleteSongDialog
import com.example.musicplayer.utils.Constant
import com.example.musicplayer.utils.Utils
import com.example.service.model.Audio
import com.example.service.utils.PlaylistUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PlaylistSongBottomSheet (private val context: Context) :
    BaseBottomSheetDialog<BottomSheetPlaylistMoreBinding>(context) {

       private  var song: Audio = Audio.EMPTY_SONG

    fun setAudio(currentAudio: Audio, idPlaylist: Int) {
        song = currentAudio
        binding.apply {
            CoroutineScope(Dispatchers.IO).launch {
                updateFavourite(song)
            }

            val string =
                song.mediaObject?.path?.let { it.substring(it.lastIndexOf('/') + 1, it.length) }
            val songTitle = song.mediaObject?.title
            val songPath = song.mediaObject?.path
            val title = if (songTitle.isNullOrEmpty()) string else songTitle

            val songArt =
                Utils.getAlbumArt(song.mediaObject?.path ?: "")


            if (songArt != null) {
                binding.songAvatar.setImageBitmap(songArt)
            } else {
                binding.songAvatar.setImageResource(R.drawable.app_logo)
            }

            binding.numberSong.text =
                if (song.artistName.isEmpty() || !song.artistName[0].isLetter() || song.artistName.contains(
                        Constant.UNKNOWN_STRING,
                        true
                    )
                ) context.resources.getString(R.string.unknown_artist) else song.artistName


            binding.songName.text = title

            songFavourite.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    val isFavourite = PlaylistUtils.getInstance(context)
                        ?.checkIsFavourite(song)
                    if (isFavourite == true) {
                        PlaylistUtils.getInstance(context)
                            ?.removeFromFavourite(song)
                    } else {
                        PlaylistUtils.getInstance(context)?.addToFavourite(song)
                    }
                    updateFavourite(song)
                    context.sendBroadcast(Intent(com.example.service.utils.Utils.ACTION_FINISH_DOWNLOAD))
                }
            }

            removeFromPlaylist.setOnClickListener {
                val deleteDialog = DeleteSongBottomSheet(context)
                deleteDialog.deleteAudioFromPlaylist(song, idPlaylist)
                deleteDialog.show()
                dismiss()
            }

            ringTone.setOnClickListener {
                song.mediaObject?.path?.let { it1 -> File(it1) }
                    ?.let { it2 -> checkAndSetRingtone(context, it2) }
            }

            share.setOnClickListener {
                com.example.service.utils.Utils.shareVideoOrAudio(context, songTitle, songPath)
                dismiss()
            }
        }
    }

    override fun getViewBinding(): BottomSheetPlaylistMoreBinding {
        return BottomSheetPlaylistMoreBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        setContentView(binding.root)
    }

    private fun checkAndSetRingtone(context: Context, audioFile: File) {
        if (!Settings.System.canWrite(context)) {
            // Ask for WRITE_SETTINGS permission
            AlertDialog.Builder(context)
                .setTitle("Permission Required")
                .setMessage("Please allow the app to modify system settings to set the ringtone.")
                .setPositiveButton("Allow") { _, _ ->
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            setAsRingtoneScoped(context, audioFile)
        }
    }

    private fun setAsRingtoneScoped(context: Context, sourceFile: File) {
        if (!Settings.System.canWrite(context)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return
        }

        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, sourceFile.name)
            put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3")
            put(MediaStore.Audio.Media.IS_RINGTONE, true)
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Ringtones/")
        }

        val audioCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val newUri = context.contentResolver.insert(audioCollection, values)

        if (newUri != null) {
            // Write file data into MediaStore
            try {
                context.contentResolver.openOutputStream(newUri).use { outputStream ->
                    sourceFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream!!)
                    }
                }

                // Set as default ringtone
                RingtoneManager.setActualDefaultRingtoneUri(
                    context,
                    RingtoneManager.TYPE_RINGTONE,
                    newUri
                )

                Toast.makeText(context, "Ringtone set successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to set ringtone", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Failed to insert audio into MediaStore", Toast.LENGTH_SHORT).show()
        }
    }


    private suspend fun updateFavourite(data: Audio) {
        val isFavourite = PlaylistUtils.getInstance(context)?.checkIsFavourite(data) ?: false
        withContext(Dispatchers.Main) {
            if (isFavourite) {
                binding.songFavourite.setImageResource(R.drawable.icon_favourite)
            } else {
                binding.songFavourite.setImageResource(R.drawable.icon_unfavourite)

            }
        }
    }


}