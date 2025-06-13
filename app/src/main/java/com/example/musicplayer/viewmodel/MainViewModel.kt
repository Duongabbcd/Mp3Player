package com.example.musicplayer.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.service.model.Album
import com.example.service.model.Artist
import com.example.service.model.Audio
import com.example.service.model.Folder
import com.example.service.model.Playlist
import com.example.service.service.SongLoader
import com.example.service.utils.DisplayMode
import com.example.service.utils.PlaylistUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@SuppressLint("StaticFieldLeak")
class MainViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {


    private val context = getApplication<Application>().applicationContext

    private val _allSongs = MutableLiveData<MutableList<Audio>>()
    val allSongs: LiveData<MutableList<Audio>>
        get() = _allSongs

    private val _allArtists = MutableLiveData<MutableList<Artist>>()
    val allArtists: LiveData<MutableList<Artist>>
        get() = _allArtists


    private val _allAlbums = MutableLiveData<MutableList<Album>>()
    val allAlbums: LiveData<MutableList<Album>>
        get() = _allAlbums

    private val _allPlaylists = MutableLiveData<MutableList<Playlist>>()
    val allPlaylists: LiveData<MutableList<Playlist>>
        get() = _allPlaylists

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _allAlbumSong = MutableLiveData<MutableList<Audio>>()
    val allAlbumSong: LiveData<MutableList<Audio>>
        get() = _allAlbumSong

    private val _allArtistSong = MutableLiveData<MutableList<Audio>>()
    val allArtistSong: LiveData<MutableList<Audio>>
        get() = _allArtistSong

    private val _allPlaylistSong = MutableLiveData<MutableList<Audio>>()
    val allPlaylistSong: LiveData<MutableList<Audio>>
        get() = _allPlaylistSong

    private val _currentPlaylist = MutableLiveData<Playlist>()
    val currentPlaylist: LiveData<Playlist>
        get() = _currentPlaylist



    private val _allFolder = MutableLiveData<MutableList<Folder>>()
    val allFolder: LiveData<MutableList<Folder>>
        get() = _allFolder

    val searchInput = MutableLiveData<String>()

    private fun loadingData(isLoading: Boolean) {
        _isLoading.value = isLoading
    }

    private val _allSelectedAudio = MutableLiveData<MutableList<Audio>>()
    val allSelectedAudio: LiveData<MutableList<Audio>>
        get() = _allSelectedAudio

    private val _allSelectedFolder = MutableLiveData<MutableList<Folder>>()
    val allSelectedFolder: LiveData<MutableList<Folder>>
        get() = _allSelectedFolder

    val hideKeyboard = MutableLiveData<Boolean>()
    val isShowSelection = MutableLiveData<Boolean>()

     val originSongs = mutableListOf<Audio>()
    private val originPlaylists = mutableListOf<Playlist>()
     val originFolders = mutableListOf<Folder>()

    private val originAlbums = mutableListOf<Album>()
    private val originArtists = mutableListOf<Artist>()

    fun getAllSongs(input: List<Audio> = listOf()) {
        originSongs.clear()
        viewModelScope.launch {
            loadingData(true)
            val current = input.ifEmpty {
                SongLoader.getAllSongs(context)
            }.toMutableList()
            originSongs.addAll(current)
            _allSongs.postValue(current)
            loadingData(false)
        }
    }

    fun getAllAlbums() {
        originAlbums.clear()
        viewModelScope.launch {
            loadingData(true)
            val allAudios = _allSongs.value ?: return@launch
            val current = SongLoader.getAlbumLocal(context, allAudios)
            originAlbums.addAll(current)
            _allAlbums.postValue(current.toMutableList())
            loadingData(false)
        }
    }


    fun getAllArtists() {
        originArtists
        viewModelScope.launch {
            loadingData(true)
            val allAudios = _allSongs.value ?: return@launch
            val current = SongLoader.getArtistLocal(context, allAudios)
            current.onEach {
                println("getAllArtists: $it")
            }
            originArtists.addAll(current)
            _allArtists.postValue(current.toMutableList())
            loadingData(false)
        }
    }

    fun getAllAlbumSong(albumId: String) {
        viewModelScope.launch {
            loadingData(true)
            val current = SongLoader.getSongAlbumById(albumId, context)
            _allAlbumSong.postValue(current)
            loadingData(false)
        }
    }

    fun getAllArtistSong(artistName: String) {
        viewModelScope.launch {
            loadingData(true)
            val current = SongLoader.getSongByArtistId(artistName, context)
            println("getAllArtistSong: $current")
            _allArtistSong.postValue(current.toMutableList())
            loadingData(false)
        }
    }

    fun getAllFolder() {
        originFolders.clear()
        viewModelScope.launch {
            loadingData(true)
            val current = SongLoader.getAllAudioFolders(context)
            originFolders.addAll(current)
            _allFolder.postValue(current.toMutableList())
            loadingData(false)
        }
    }

    fun getAllPlaylists() {
        originPlaylists.clear()
        viewModelScope.launch {
            loadingData(true)
            val playList =
                PlaylistUtils.getInstance(context)?.getPlaylists() ?: arrayListOf()

            originPlaylists.addAll(playList)
            _allPlaylists.postValue(playList.toMutableList())
            loadingData(false)
        }
    }

    fun searchText(input: String, displayMode: DisplayMode) {
        searchInput.value = input
        when (displayMode) {
            DisplayMode.AUDIO -> {
                if(input.isEmpty()) {
                   _allSongs.postValue(originSongs)
                } else {
                    loadingData(true)
                    val result = originSongs.filter { it.mediaObject?.title?.contains(input, ignoreCase = true) == true}
                    _allSongs.postValue(result.toMutableList())
                    loadingData(false)
                }
            }

            DisplayMode.PLAYLIST -> {
                if(input.isEmpty()) {
                    _allPlaylists.postValue(originPlaylists)
                } else {
                    loadingData(true)
                    val result = originPlaylists.filter { it.title.contains(input, ignoreCase = true) }
                    _allPlaylists.postValue(result.toMutableList())
                    loadingData(false)
                }
            }


            DisplayMode.FOLDER -> {
                if(input.isEmpty()) {
                    _allFolder.postValue(originFolders)
                } else {
                    loadingData(true)
                    val result = originFolders.filter { it.name.contains(input, ignoreCase = true) }
                    _allFolder.postValue(result.toMutableList())
                    loadingData(false)
                }
            }


            DisplayMode.ALBUM -> {
                if(input.isEmpty()) {
                    _allAlbums.postValue(originAlbums)
                } else {
                    loadingData(true)
                    val result = originAlbums.filter { it.albumName.contains(input, ignoreCase = true) }
                    _allAlbums.postValue(result.toMutableList())
                    loadingData(false)
                }
            }

            DisplayMode.ARTIST -> {
                if(input.isEmpty()) {
                    _allArtists.postValue(originArtists)
                } else {
                    loadingData(true)
                    val result = originArtists.filter { it.nameArtist.contains(input, ignoreCase = true) }
                    _allArtists.postValue(result.toMutableList())
                    loadingData(false)
                }
            }

            else -> {
                if(input.isEmpty()) {
                    _allSongs.postValue(originSongs)
                } else {
                    loadingData(true)
                    val result = originSongs.filter { it.albumName.contains(input, ignoreCase = true) }
                    _allSongs.postValue(result.toMutableList())
                    loadingData(false)
                }
            }
        }
    }


    fun checkKeyboard(isHidden: Boolean = false) {
        hideKeyboard.value = isHidden
    }


    fun checkSelection(isShowing: Boolean = false) {
        isShowSelection.value = isShowing
    }

    fun getSelectedAudios(audio: Audio) {
        val current = _allSelectedAudio.value ?: mutableListOf()
        if(!current.contains(audio)) {
            current.add(audio)
        } else {
            current.remove(audio)
        }

        _allSelectedAudio.postValue(current)
    }

    fun getSelectedFolders(folder: Folder) {
        val current = _allSelectedFolder.value ?: mutableListOf()
        if(!current.contains(folder)) {
            current.add(folder)
        } else {
            current.remove(folder)
        }

        _allSelectedFolder.postValue(current)
    }

    fun getAllSelectedAudios(audios : List<Audio>) {
        _allSelectedAudio.postValue(audios.toMutableList())
    }
 fun getAllSelectedFolders(folders : List<Folder>) {
        _allSelectedFolder.postValue(folders.toMutableList())
    }

    fun getPlaylist(id: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val current = PlaylistUtils.getInstance(context)?.getPlaylistById(id) ?: return@launch
            println("getPlaylist: $current")
            _currentPlaylist.postValue(current)
            _allPlaylistSong.postValue(current.tracks)
        }
    }


}