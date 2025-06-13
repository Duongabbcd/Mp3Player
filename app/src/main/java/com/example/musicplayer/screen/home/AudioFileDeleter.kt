package com.example.musicplayer.screen.home

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.io.File

object AudioFileDeleter {

    private const val TAG = "AudioFileDeleter"

    fun deleteAudioFile(context: Context, filePath: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ use MediaStore deletion
            deleteViaMediaStore(context, filePath)
        } else {
            // Android 9 or below: delete with File.delete()
            deleteViaFileApi(filePath)
        }
    }

    private fun deleteViaMediaStore(context: Context, filePath: String): Boolean {
        val contentUri = getAudioContentUri(context, filePath)

        return if (contentUri != null) {
            try {
                val rowsDeleted = context.contentResolver.delete(contentUri, null, null)
                Log.d(TAG, "Deleted via MediaStore: $rowsDeleted rows for $filePath")
                rowsDeleted > 0
            } catch (se: SecurityException) {
                Log.e(TAG, "Permission denied to delete audio via MediaStore: $filePath", se)
                // fallback to file delete (may fail on scoped storage)
                deleteViaFileApi(filePath)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting audio via MediaStore: $filePath", e)
                false
            }
        } else {
            Log.w(TAG, "Audio file not found in MediaStore, fallback to File.delete(): $filePath")
            deleteViaFileApi(filePath)
        }
    }

    private fun getAudioContentUri(context: Context, filePath: String): Uri? {
        val projection = arrayOf(MediaStore.Audio.Media._ID)
        val selection = "${MediaStore.Audio.Media.DATA} = ?"
        val selectionArgs = arrayOf(filePath)
        val queryUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        context.contentResolver.query(queryUri, projection, selection, selectionArgs, null).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val id = cursor.getLong(idIndex)
                return ContentUris.withAppendedId(queryUri, id)
            }
        }
        return null
    }

    private fun deleteViaFileApi(filePath: String): Boolean {
        val file = File(filePath)
        if (!file.exists()) {
            Log.w(TAG, "File does not exist: $filePath")
            return false
        }

        return try {
            val deleted = file.delete()
            Log.d(TAG, "Deleted via File API: $deleted for $filePath")
            deleted
        } catch (e: SecurityException) {
            Log.e(TAG, "Permission denied deleting file: $filePath", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file: $filePath", e)
            false
        }
    }
}