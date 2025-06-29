package com.example.service.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.text.DecimalFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

object Utils {

    fun getDuration(duration: Long): String {
        val hms: String
        if (duration > 3600000) {
            hms = String.format(
                Locale.US, "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(duration),
                TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(
                    TimeUnit.MILLISECONDS.toHours(
                        duration
                    )
                ),
                TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(
                        duration
                    )
                )
            )
        } else {
            hms = String.format(
                Locale.US, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(
                    TimeUnit.MILLISECONDS.toHours(
                        duration
                    )
                ),
                TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(
                        duration
                    )
                )
            )
        }

        return hms
    }

    fun getPathDownload(): File {
        val rootFile: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        } else {
            Environment.getExternalStorageDirectory()
        }
        return File("$rootFile/MusicDownload")
    }

    fun showFileSize(size: Long): String {
        val sizeString : String
        if (size < 1024000) {
            sizeString = (size / 1000).toString() + "kb"
        } else {
            val mmm = ((size / 1024).toString() + "").toInt()
            val f = DecimalFormat("#,###,###")
            sizeString = f.format(mmm.toLong()) + " MB"
        }
        return sizeString
    }

    fun shareVideoOrAudio(context: Context, title: String?, filePath: String?) {
        if (filePath.isNullOrEmpty()) {
            Log.e("Share", "File path is null or empty.")
            return
        }

        val file = File(filePath)
        if (!file.exists()) {
            Log.e("Share", "File does not exist at path: $filePath")
            return
        }

        try {
            // Use FileProvider to get a content URI
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider", // must match manifest authority
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "*/*" // You can customize this depending on file type
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TITLE, title)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share file via")
            if (context !is Activity) {
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)

        } catch (e: Exception) {
            Log.e("Share", "Error sharing file: ${e.message}", e)
        }
    }

    fun shareAudioList(context: Context, paths: ArrayList<String>) {
        val uriList = arrayListOf<Uri>()

        //Convert path into Uri
        for (path in paths) {
            val uri = Uri.fromFile(File(path))
            uriList.add(uri)
        }

        //Create intent to share list sound files
        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
        shareIntent.setType("audio/*")
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        if (context is Activity) {
            context.startActivity(Intent.createChooser(shareIntent, "Share audio file by"))
        }
    }

    fun checkForInternet(context: Context): Boolean {

        // register activity with the connectivity manager service
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // if the android version is equal to M
        // or greater we need to use the
        // NetworkCapabilities to check what type of
        // network has the internet connection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Returns a Network object corresponding to
            // the currently active default data network.
            println("activeNetwork 0: ${connectivityManager.activeNetwork} ")
            val network = connectivityManager.activeNetwork ?: return false
            println("activeNetwork 1: ${connectivityManager.getNetworkCapabilities(network)}")
            // Representation of the capabilities of an active network.
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                // Indicates this network uses a Wi-Fi transport,
                // or WiFi has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                // Indicates this network uses a Cellular transport. or
                // Cellular has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                // else return false
                else -> false
            }
        } else {
            // if the android version is below M
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    fun getAlbumArt(filePath: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        try {
            // Set the data source to the MP3 file
            retriever.setDataSource(filePath)

            // Retrieve the album art (if any)
            val art = retriever.embeddedPicture
            println("art is null $art")

            // Return album art if it exists, otherwise null
            if (art != null) {
                return BitmapFactory.decodeByteArray(art, 0, art.size)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }
        return null  // No album art found
    }


    const val ODER_SORT_FILE_DEFAULT = MediaStore.Audio.Media.DEFAULT_SORT_ORDER
    const val ODER_SORT_TITLE_ASC = MediaStore.Audio.Media.TITLE + " ASC"

    const val ACTION_FINISH_DOWNLOAD = "ACTION_FINISH_DOWNLOAD"
}

enum class DisplayMode() {
    ARTIST,
    ALBUM,
    AUDIO,
    PLAYLIST,
    PLAYING_SONG,
    DETAIL,
    FOLDER
}