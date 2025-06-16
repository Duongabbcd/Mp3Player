package com.example.service.service

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import com.example.service.model.Album
import com.example.service.model.Artist
import com.example.service.model.Audio
import com.example.service.model.Folder
import com.example.service.model.MediaObject
import com.example.service.utils.PreferenceUtil
import com.example.service.utils.Utils

object SongLoader {

    private const val AUDIO_SELECTION =
        MediaStore.Audio.Media.IS_MUSIC + "!= 0 AND " + MediaStore.Audio.Media.MIME_TYPE + " LIKE 'audio/%'"

    private val AUDIO_PROJECTION = arrayOf(
        MediaStore.Audio.Media._ID, // 0
        MediaStore.Audio.Media.TITLE, // 1,
        MediaStore.Audio.Media.TRACK, // 2,
        MediaStore.Audio.Media.YEAR, // 3,
        MediaStore.Audio.Media.DURATION, // 4, // 5,
        MediaStore.Audio.Media.DATE_MODIFIED, // 6,
        MediaStore.Audio.Media.ALBUM_ID, // 7,
        MediaStore.Audio.Media.ALBUM, // 8,
        MediaStore.Audio.Media.ARTIST_ID, // 9,
        MediaStore.Audio.Media.ARTIST, // 10
        MediaStore.Audio.Media.DISPLAY_NAME, // 10
        MediaStore.Audio.Media.SIZE, // 10
        MediaStore.Audio.Media.DATE_ADDED, // 10
    )
    private const val UNKNOWN = "Unknown"


    fun getAllSongs(context: Context): ArrayList<Audio> {
        val cursor = makeSongCursor(context, null, null)
        return getSongs(cursor)
    }

    fun getSongs(cursor: Cursor?): ArrayList<Audio> {
        val songs = arrayListOf<Audio>()
        println("getSongs: ${cursor == null}")
        println("getSongs: ${cursor?.moveToFirst()}")
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val song = getSongFromCursorImpl(cursor)
                if(song.timeSong != "00:00") {
                    songs.add(song)
                }
            } while (cursor.moveToNext())
        }

        cursor?.close()
        return songs
    }

    private fun getSongFromCursorImpl(cursor: Cursor): Audio {
        val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
        val titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
        val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
        val dateModifiedIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
        val albumIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
        val albumNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
        val artistIdIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
        val artistNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)

        val id = cursor.getLong(idIndex)
        val title = cursor.getString(titleIndex)
        val duration = cursor.getLong(durationIndex)
        val dateModified = cursor.getLong(dateModifiedIndex)
        val albumId = cursor.getInt(albumIdIndex)
        val albumName = cursor.getString(albumNameIndex)
        val artistId = cursor.getInt(artistIdIndex)
        val artistName = cursor.getString(artistNameIndex)

        // ✅ Get content URI
        val contentUri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toLong()
        )

        return Audio(
            albumId = albumId.toString(),
            albumName = albumName,
            timeSong = Utils.getDuration(duration),
            artistId = artistId.toString(),
            artistName = artistName,
            timeCount = duration,
            mediaObject = MediaObject(
                id.toString(),
                title,
                "",
                contentUri.toString(),
                timeModified = dateModified
            )
        ).also {
            println("audio: $it")
        }
    }

    fun makeSongCursor(
        context: Context,
        selection: String?,
        selectionValues: Array<String>?
    ): Cursor? {
        return makeSongCursor(
            context,
            selection,
            selectionValues,
            PreferenceUtil.getInstance(context)?.getSongSortOrder()
        )
    }

    private fun makeSongCursor(
        context: Context,
        selection: String?,
        selectionValues: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val customSelection = if (selection != null && selection.trim() != "") {
            "$AUDIO_SELECTION AND $selection"
        } else AUDIO_SELECTION

        return try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, null, null, sortOrder
            )
        } catch (e: SecurityException) {
            null
        }
    }


    fun getSongAlbumById(albumId: String, context: Context): ArrayList<Audio> {
        val songList = arrayListOf<Audio>()
        try {
            println("albumId: $albumId")
            val selection =
                "${MediaStore.Audio.Media.ALBUM}=? AND ${MediaStore.Audio.Media.DURATION}>0"
            val selectionArgs = arrayOf(albumId) // ✅ NOT artistId

            val AUDIO_BY_ARTIST_PROJECTION = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM
            )


            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                AUDIO_BY_ARTIST_PROJECTION,
                selection,
                selectionArgs,
                PreferenceUtil.getInstance(context)?.getSongSortOrder()
            )?.use { cursor ->

                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val artistNameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val artistIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dateModCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val albumNameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)

                while (cursor.moveToNext()) {

                    val songId = cursor.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId
                    )

                    val title = cursor.getString(nameCol)
                    val artistName = cursor.getString(artistNameCol)
                    val artistId = cursor.getString(artistIdCol)
                    val duration = cursor.getLong(durationCol)
                    val size = cursor.getLong(sizeCol)
                    val dateMod = cursor.getLong(dateModCol)
                    val albumId = cursor.getLong(albumIdCol).toString()
                    val albumName = cursor.getString(albumNameCol)
                    val timeSongTxt = Utils.getDuration(duration)

                    if (timeSongTxt != "00:00") {
                        songList += Audio(
                            artistId = artistId,
                            artistName = artistName,
                            albumId = albumId,
                            albumName = albumName,
                            timeSong = timeSongTxt,
                            timeCount = duration,
                            mediaObject = MediaObject(
                                id = songId.toString(),
                                title = title,
                                path = contentUri.toString(),
                                fileSize = size,
                                timeModified = dateMod
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return songList
    }

    fun getAlbumLocal(context: Context, allAudios: MutableList<Audio>): List<Album> {
        val albumMap = mutableMapOf<Long, MutableList<Audio>>()

        for (song in allAudios) {
            val list = albumMap.getOrPut(song.albumId.toLong()) { mutableListOf() }
            list.add(song)
        }

        return albumMap.map { (albumId, albumSongs) ->
            Album(
                albumId = albumId.toString(),
                albumName = albumSongs.first().albumName,
                numberSong = albumSongs.size.toString(),
                artistKey = albumSongs.first().artistName
            )
        }
    }

    fun getArtistLocal(context: Context, songs: List<Audio>): List<Artist> {
        val artistMap = mutableMapOf<Long, MutableList<Audio>>()

        for (song in songs) {
            val artistIdLong = song.artistId.toLongOrNull() ?: continue
            if (song.artistName.isBlank() || song.timeSong == "00:00") continue

            val list = artistMap.getOrPut(artistIdLong) { mutableListOf() }
            list.add(song)
        }

        return artistMap.map { (artistId, artistSongs) ->
            Artist(
                artistId = artistId.toString(),
                nameArtist = artistSongs.first().artistName,
                numberSong = artistSongs.size.toString(),
                numberOfAlbum = artistSongs.map { it.albumId }.distinct().size.toString()
            )
        }
    }

    fun getSongByArtistId(artistId: String, context: Context): List<Audio> {
        val songs = mutableListOf<Audio>()
        try {
            val selection = "${MediaStore.Audio.Media.ARTIST}=? AND ${MediaStore.Audio.Media.DURATION}>0"
            val selectionArgs = arrayOf(artistId) // ✅ NOT artistId

            val AUDIO_BY_ARTIST_PROJECTION = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATE_ADDED,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ALBUM
            )

            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                AUDIO_BY_ARTIST_PROJECTION,
                selection,
                selectionArgs,
                PreferenceUtil.getInstance(context)?.getSongSortOrder()
            )?.use { cursor ->

                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val artistNameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dateModCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val albumNameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)

                while (cursor.moveToNext()) {

                    val songId = cursor.getLong(idCol)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId
                    )

                    val title = cursor.getString(nameCol)
                    val artistName = cursor.getString(artistNameCol)
                    val duration = cursor.getLong(durationCol)
                    val size = cursor.getLong(sizeCol)
                    val dateMod = cursor.getLong(dateModCol)
                    val albumId = cursor.getLong(albumIdCol).toString()
                    val albumName = cursor.getString(albumNameCol)
                    val timeSongTxt = Utils.getDuration(duration)

                    if (timeSongTxt != "00:00") {
                        songs += Audio(
                            artistId = artistId,
                            artistName = artistName,
                            albumId = albumId,
                            albumName = albumName,
                            timeSong = timeSongTxt,
                            timeCount = duration,
                            mediaObject = MediaObject(
                                id = songId.toString(),
                                title = title,
                                path = contentUri.toString(),
                                fileSize = size,
                                timeModified = dateMod
                            )
                        )
                    }
                }
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }
        return songs
    }


    fun getAllAudioFolders(context: Context): List<Folder> {
        val foldersMap = mutableMapOf<String, MutableList<Audio>>()

        try {
            val allSongs = getAllSongs(context)  // Your existing function to get List<Audio>

            allSongs.forEach {
                println("Audio filePath: ${it.mediaObject?.path}")
            }

            val filteredSongs = allSongs.filter { !it.mediaObject?.path.isNullOrEmpty() }

            val grouped = filteredSongs.groupBy { audio ->
                try {
                    val uri = Uri.parse(audio.mediaObject?.path)
                    val folderName = getFolderNameFromUri(context, uri)
                    println("Grouping by folderName: $folderName")
                    folderName
                } catch (e: Exception) {
                    println("Exception in groupBy: ${e.message}")
                    "Unknown"
                }
            }

            println("grouped keys: ${grouped.keys}")
            println("grouped size: ${grouped.size}")

            grouped.forEach { (folderName, audioList) ->
                foldersMap[folderName] = audioList.toMutableList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return foldersMap.map { (folderName, tracks) ->
            Folder(name = folderName, path = "", tracks = tracks)
        }
    }


    fun getFolderNameFromUri(context: Context, uri: Uri): String {
        // Try 1: Use DocumentFile to get parent folder name
        val docFile = DocumentFile.fromSingleUri(context, uri)
        val parentDoc = docFile?.parentFile
        if (parentDoc != null) {
            return parentDoc.name ?: "Unknown"
        }

        // Try 2: Query MediaStore for album name as folder substitute
        val projection = arrayOf(MediaStore.Audio.Media.ALBUM)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumName = cursor.getString(albumIndex)
                if (!albumName.isNullOrEmpty()) {
                    return albumName
                }
            }
        }

        // Fallback
        return "Unknown"
    }

    fun getArtistsInDevice(context: Context): List<Artist> {
        val list = arrayListOf<Artist>()
        try {
            val artistCursor = context.contentResolver.query(
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.Artists.DEFAULT_SORT_ORDER
            )
            if (artistCursor != null) {
                artistCursor.moveToFirst()
                val artistId = artistCursor.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID)
                val artistName = artistCursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)
                val numberOfTracks =
                    artistCursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)
                val numberOfAlbum =
                    artistCursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)
                do {
                    val artist = Artist(
                        artistCursor.getString(artistId),
                        artistCursor.getString(artistName),
                        artistCursor.getString(numberOfTracks),
                        artistCursor.getString(numberOfAlbum),
                    )
                    list.add(artist)
                } while (artistCursor.moveToNext())
                artistCursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

}
