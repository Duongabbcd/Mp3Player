package com.example.service.service

import android.content.Context
import android.database.Cursor
import android.provider.BaseColumns
import android.provider.MediaStore
import com.example.service.model.Album
import com.example.service.model.Artist
import com.example.service.model.Audio
import com.example.service.model.Folder
import com.example.service.model.MediaObject
import com.example.service.utils.PreferenceUtil
import com.example.service.utils.Utils
import java.io.File

object SongLoader {

    private const val AUDIO_SELECTION =
        MediaStore.Audio.Media.IS_MUSIC + "!= 0 AND " + MediaStore.Audio.Media.MIME_TYPE + " LIKE 'audio/%'"

    private val AUDIO_PROJECTION = arrayOf(
        BaseColumns._ID, // 0
        MediaStore.Audio.Media.TITLE, // 1,
        MediaStore.Audio.Media.TRACK, // 2,
        MediaStore.Audio.Media.YEAR, // 3,
        MediaStore.Audio.Media.DURATION, // 4,
        MediaStore.Audio.Media.DATA, // 5,
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
        val id = cursor.getString(0)
        val title = cursor.getString(1)
        val trackNumber = cursor.getInt(2)
        val year = cursor.getInt(3)
        val duration = cursor.getLong(4)
        val data = cursor.getString(5)
        val dateModified = cursor.getLong(6)
        val albumId = cursor.getInt(7)
        val albumName = cursor.getString(8)
        val artistId = cursor.getInt(9)
        val artistName = cursor.getString(10)

        return Audio(
            albumId = albumId.toString(),
            albumName = albumName,
            timeSong = Utils.getDuration(duration),
            artistId = artistId.toString(),
            artistName = artistName,
            timeCount = duration,
            mediaObject = MediaObject(id.toString(), title, "", data, timeModified = dateModified)
        )
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
                AUDIO_PROJECTION, customSelection, selectionValues, sortOrder
            )
        } catch (e: SecurityException) {
            null
        }
    }

    private fun makeAlbumSongCursorById(albumId: String, context: Context): Cursor? {
        val selection = StringBuilder()
        selection.append("${MediaStore.Audio.AudioColumns.IS_MUSIC}=1")
        selection.append(" AND ${MediaStore.Audio.AudioColumns.TITLE} != ''")
        selection.append(" AND ${MediaStore.Audio.AudioColumns.ALBUM} = '${albumId.replace("'", "''")}'")

        return context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            null,
            selection.toString(),
            null,
            "${MediaStore.Audio.Media.TRACK}, ${PreferenceUtil.getInstance(context)?.getSongSortOrder()}"
        )
    }

    fun getSongAlbumById(albumId: String, context: Context): ArrayList<Audio> {
        val songList = arrayListOf<Audio>()
        try {
            println("albumId: $albumId")
            val audioCursor = makeAlbumSongCursorById(albumId, context)
            if (audioCursor != null && audioCursor.moveToFirst()) {

                val id = audioCursor.getColumnIndexOrThrow(BaseColumns._ID)
                val nameIndex = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val artistNameIndex = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val artistIndex = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
                val uriIndex = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val durationIndex = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeIndex = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dateIndex = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val albumIndex = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val albumNameIndex = audioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)

                do {
                    val audioId = audioCursor.getInt(id).toString()
                    val path = audioCursor.getString(uriIndex)
                    val title = audioCursor.getString(nameIndex)
                    val artist = audioCursor.getString(artistNameIndex)
                    val artistId = audioCursor.getString(artistIndex)
                    val album = audioCursor.getString(albumIndex)
                    val timeSong = Utils.getDuration(audioCursor.getLong(durationIndex))
                    val timeCount = audioCursor.getLong(durationIndex)
                    val fileSize = audioCursor.getLong(sizeIndex)
                    val timeModified = audioCursor.getLong(dateIndex)
                    val albumName = audioCursor.getString(albumNameIndex)
                    val audio = Audio(
                        artistId = artistId,
                        artistName = artist,
                        timeSong = timeSong,
                        albumId = album,
                        albumName = albumName,
                        timeCount = timeCount,
                        mediaObject = MediaObject(id.toString(), title, "", path, fileSize = fileSize)
                    )
                    if(timeSong != "00:00") {
                        songList.add(audio)
                    }

                } while (audioCursor.moveToNext())
                audioCursor.close()
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
        val artistList = arrayListOf<Audio>()
        try {
            val artistCursor = makeArtistAlbumCursor(artistId, context)
            println("getSongByArtistId is null 1 ${artistCursor == null}")
            println("getSongByArtistId is null 2 ${artistCursor?.moveToFirst()}")
            if (artistCursor != null && artistCursor.moveToFirst()) {
                println("getSongByArtistId is here")
                val id = artistCursor.getColumnIndexOrThrow(BaseColumns._ID)
                val nameIndex = artistCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val artistIndex = artistCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
                val artistNameIndex = artistCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val uriIndex = artistCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val durationIndex = artistCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeIndex = artistCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                val dateIndex = artistCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
                val albumIndex = artistCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val albumNameIndex = artistCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)

                do {
                    val audioId = artistCursor.getInt(id).toString()
                    val path = artistCursor.getString(uriIndex)
                    val title = artistCursor.getString(nameIndex)
                    val artist = artistCursor.getString(artistNameIndex)
                    val artistId = artistCursor.getString(artistIndex)
                    val imageThumb = artistCursor.getString(albumIndex)
                    val timeSong = Utils.getDuration(artistCursor.getLong(durationIndex))
                    val timeCount = artistCursor.getLong(durationIndex)
                    val fileSize = artistCursor.getLong(sizeIndex)
                    val timeModified = artistCursor.getLong(dateIndex)
                    val albumId = artistCursor.getString(albumIndex)
                    val albumName = artistCursor.getString(albumNameIndex)
                    val audio = Audio(
                        artistId = artistId,
                        artistName = artist,
                        timeSong = timeSong,
                        albumName = albumName,
                        albumId = albumId,
                        timeCount = timeCount,
                        mediaObject = MediaObject(id.toString(), title, "", path, fileSize = fileSize)
                    )

                    println("getSongByArtistId: $audio")
                    if(timeSong != "00:00") {
                        artistList.add(audio)
                    }
                } while (artistCursor.moveToNext())
                artistCursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return artistList
    }

    private fun makeArtistAlbumCursor(artistId: String, context: Context): Cursor? {
        try {
            val selection = "${MediaStore.Audio.Media.ARTIST}=? AND ${MediaStore.Audio.Media.DURATION}>0"
            val selectionArgs = arrayOf(artistId.toString())

            return context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                AUDIO_PROJECTION, // ✅ MUST include ARTIST_ID
                selection,
                selectionArgs,
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }


    fun getAllAudioFolders(context: Context): List<Folder> {
        val foldersMap = mutableMapOf<String, MutableList<Audio>>()

        val projection = arrayOf(
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val cursor = context.contentResolver.query(uri, projection, selection, null, null)

        cursor?.use {
            val dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val artistNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val artistIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val albumNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)

            while (cursor.moveToNext()) {
                val filePath = cursor.getString(dataIndex) ?: continue
                val file = File(filePath)
                if (!file.exists()) continue

                val durationMs = cursor.getLong(durationIndex)
                if (durationMs <= 0) continue

                val folderFile = file.parentFile ?: continue
                val folderPath = folderFile.absolutePath

                val audio = Audio(
                    artistName = cursor.getString(artistNameIndex) ?: "",
                    artistId = cursor.getString(artistIndex) ?: "",
                    timeSong = Utils.getDuration(durationMs),
                    albumName = cursor.getString(albumNameIndex) ?: "",
                    albumId = cursor.getString(albumIndex) ?: "",
                    timeCount = durationMs,
                    filePath = filePath
                )

                foldersMap.getOrPut(folderPath) { mutableListOf() }.add(audio)
            }
        }

        // Convert to Folder objects, only if valid audio files exist
        return foldersMap.mapNotNull { (path, audioList) ->
            if (audioList.isEmpty()) return@mapNotNull null
            val folderName = File(path).name
            Folder(name = folderName, path = path, tracks = audioList)
        }
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
