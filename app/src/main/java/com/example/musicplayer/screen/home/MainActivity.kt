package com.example.musicplayer.screen.home

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.TextView
import androidx.activity.result.IntentSenderRequest
import androidx.activity.viewModels
import com.example.musicplayer.R
import com.example.musicplayer.base.BaseActivity
import com.example.musicplayer.databinding.ActivityMainBinding
import com.example.musicplayer.screen.home.fragment.AlbumFragment
import com.example.musicplayer.screen.home.fragment.ArtistFragment
import com.example.musicplayer.screen.home.fragment.FolderFragment
import com.example.musicplayer.screen.home.fragment.PlaylistFragment
import com.example.musicplayer.screen.home.fragment.SongFragment
import com.example.musicplayer.screen.setting.SettingActivity
import com.example.musicplayer.utils.Common.gone
import com.example.musicplayer.utils.Common.visible
import com.example.musicplayer.utils.Utils.hideKeyBoard
import com.example.musicplayer.utils.Utils.showKeyboard
import com.example.musicplayer.viewmodel.MainViewModel
import com.example.service.model.Audio
import com.example.service.model.Folder
import com.example.service.service.MusicPlayerRemote
import com.example.service.service.MusicService
import com.example.service.utils.DisplayMode
import com.example.service.utils.MusicUtil
import com.example.service.utils.PermissionUtils
import com.example.service.utils.Utils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate),
    SongFragment.OnButtonClickListener, FolderFragment.OnSelectFolderListener {
    private val mainViewModel: MainViewModel by viewModels()

    private val musicPlayerRemote = MusicPlayerRemote
    private var serviceToken: MusicPlayerRemote.ServiceToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initServicePlay()

        binding.apply {
            setting.setOnClickListener {
                startActivity(Intent(this@MainActivity, SettingActivity::class.java))
            }

            search.setOnClickListener {
                searchBar.visible()
                selectBar.gone()
                topBar.gone()
                mainViewModel.checkKeyboard(false)
            }


            song.setOnClickListener {
                displayMode = DisplayMode.AUDIO
                updateDisplayMode()
                selectedTab = 0
                displayScreen()
            }

            playlist.setOnClickListener {
                displayMode = DisplayMode.PLAYLIST
                updateDisplayMode()
               selectedTab = 1
                displayScreen()
            }

            folder.setOnClickListener {
                displayMode = DisplayMode.FOLDER
                updateDisplayMode()
                selectedTab = 2
                displayScreen()
            }

            albums.setOnClickListener {
                displayMode = DisplayMode.ALBUM
                updateDisplayMode()
                selectedTab = 3
                displayScreen()
            }

            artists.setOnClickListener {
                displayMode = DisplayMode.ARTIST
                updateDisplayMode()
                selectedTab = 4
                displayScreen()
            }


        }
    }

    private fun displayScreen() {
        println("displayScreen: $selectedTab")
        when(selectedTab) {
            0 -> {
                mainViewModel.getAllSongs()
                mainViewModel.getAllSelectedAudios(listOf())
                openFragment(SongFragment.newInstance())
            }
            1 -> {
                mainViewModel.getAllPlaylists()
                openFragment(PlaylistFragment.newInstance())

            }
            2 -> {
                mainViewModel.getAllFolder()
                openFragment(FolderFragment.newInstance())
            }
            3 -> {
                mainViewModel.getAllAlbums()
                openFragment(AlbumFragment.newInstance())
            }
            4 -> {
                mainViewModel.getAllArtists()
                openFragment(ArtistFragment.newInstance())
            }
            else -> {
                mainViewModel.getAllSongs()
                mainViewModel.getAllSelectedAudios(listOf())
                openFragment(SongFragment.newInstance())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        displayScreen()
        binding.apply {
            if(!PermissionUtils.havePermission(this@MainActivity)) {
                noPermission.visible()
                setting.isEnabled = false
                search.isEnabled = false
                permissionBtn.setOnClickListener {
                    PermissionUtils.requestPermission(this@MainActivity)
                }
            } else {
                noPermission.gone()
                setting.isEnabled = true
                search.isEnabled = true
                updateDisplayMode()

            }

            searchText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    // Before text is changed
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Text is changing
                }

                override fun afterTextChanged(s: Editable?) {
                    // After text has changed
                    val input = s.toString()
                    mainViewModel.searchText(input, displayMode)
                }
            })

            backBtn.setOnClickListener {
                mainViewModel.searchText("", displayMode)
                mainViewModel.getAllSelectedFolders(listOf())
                searchBar.gone()
                topBar.visible()
                mainViewModel.searchInput.value = ""
                mainViewModel.checkKeyboard(true)
            }

            mainViewModel.hideKeyboard.observe(this@MainActivity) { result ->
                if(result) {
                    this@MainActivity.hideKeyBoard(binding.searchText)
                } else {
                    this@MainActivity.showKeyboard(binding.searchText)
                }
            }

            val hintText = when(displayMode) {
                DisplayMode.AUDIO -> resources.getString(R.string.search_song_name)
                DisplayMode.ALBUM -> resources.getString(R.string.search_album_name)
                DisplayMode.ARTIST -> resources.getString(R.string.search_artist_name)
                DisplayMode.FOLDER -> resources.getString(R.string.search_folder_name)
                DisplayMode.PLAYLIST -> resources.getString(R.string.search_playlist_name)
                else -> resources.getString(R.string.search_song_name)
            }

            searchText.hint = hintText

            selectBackBtn.setOnClickListener {
                mainViewModel.checkSelection(false)
            }

            mainViewModel.isShowSelection.observe(this@MainActivity) { result ->
                if(result) {
                    selectBar.visible()
                    topBar.gone()
                    topController.gone()
                } else {
                    selectBar.gone()
                    topBar.visible()
                    topController.visible()
                }
            }

            mainViewModel.allSelectedAudio.observe(this@MainActivity) { items ->
                allSelected.text = items.size.toString().plus(" ${resources.getString(R.string.selected)}")
                if(items.size == mainViewModel.originSongs.size) {
                    selectAllBtn.setOnClickListener {
                        mainViewModel.getAllSelectedAudios(listOf())
                    }
                    selectAllBtn.setImageResource(R.drawable.checkbox_sorting_select)
                } else {
                    selectAllBtn.setImageResource(R.drawable.checkbox_un_select)
                    selectAllBtn.setOnClickListener {
                        mainViewModel.getAllSelectedAudios(mainViewModel.originSongs)
                    }
                }
            }

            mainViewModel.allSelectedFolder.observe(this@MainActivity) { items ->
                allFolderSelected.text = items.size.toString().plus(" ${resources.getString(R.string.selected)}")
                if (items.size == mainViewModel.originFolders.size) {
                    selectAllFolderBtn.setOnClickListener {
                        mainViewModel.getAllSelectedFolders(listOf())
                    }
                    selectAllFolderBtn.setImageResource(R.drawable.checkbox_sorting_select)
                } else {
                    selectAllFolderBtn.setImageResource(R.drawable.checkbox_un_select)
                    selectAllFolderBtn.setOnClickListener {
                        mainViewModel.getAllSelectedFolders(mainViewModel.originFolders)
                    }
                }
            }

            selectAllTitle.setOnClickListener {
                mainViewModel.getAllSelectedAudios(mainViewModel.originSongs)
            }

        }
    }

    private fun updateDisplayMode() {
        binding.apply {
            when (displayMode) {
                DisplayMode.AUDIO -> {
                    setBackgroundResource(song, listOf(albums, artists, folder, playlist))
                    setTextColor(song, listOf(albums, artists, folder, playlist))
                }

                DisplayMode.ALBUM -> {
                    setBackgroundResource(albums, listOf(song, artists, folder, playlist))
                    setTextColor(albums, listOf(song, artists, folder, playlist))

                }

                DisplayMode.ARTIST -> {
                    setBackgroundResource(artists, listOf(albums, song, folder, playlist))
                    setTextColor(artists, listOf(albums, song, folder, playlist))
                }

                DisplayMode.PLAYLIST -> {
                    setBackgroundResource(playlist, listOf(albums, artists, song, folder))
                    setTextColor(playlist, listOf(albums, artists, song, folder))
                }

                DisplayMode.FOLDER -> {
                    setBackgroundResource(folder, listOf(albums, artists, playlist, song))
                    setTextColor(folder, listOf(albums, artists, playlist, song))
                }
                else -> {
                    setBackgroundResource(song, listOf(albums, artists, folder, playlist))
                    setTextColor(song, listOf(albums, artists, folder, playlist))
                }
            }
        }
    }

    private fun setBackgroundResource(selected: TextView, unselected: List<TextView>) {
        selected.setBackgroundResource(R.drawable.background_selected_100dp)
        unselected.onEach {
            it.setBackgroundResource(R.drawable.background_unselected_100dp)
        }
    }

    private fun setTextColor(selected: TextView, unselected: List<TextView>) {
        selected.setTextColor(resources.getColor(R.color.white))
        unselected.onEach {
            it.setTextColor(resources.getColor(R.color.top_selected))
        }
    }

    private fun initServicePlay() {
        serviceToken = musicPlayerRemote.bindToService(this, object : ServiceConnection {
            override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
            }

        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }

    override fun onDestroy() {
        if (isChangeTheme) {
            isChangeTheme = false
        } else {
            try {
                musicPlayerRemote.unbindFromService(serviceToken)
            } catch (e: Exception) {
                Log.e("unbindFromService", "$e")
            }

            try {

                val intent = Intent(this, MusicService::class.java)
                stopService(intent)
            } catch (e: Exception) {
                Log.e("stopService", "$e")
            }
        }
        super.onDestroy()

    }

    override fun onStart() {
        super.onStart()
        if (isChangeTheme) {
            recreate()
        }
    }


    companion object {
        var selectedTab = 0
        var isChangeTheme =  false
        var displayMode = DisplayMode.AUDIO

    }

    override fun onButtonClicked() {
        binding.searchBar.gone()

        binding.allFolderSelected.gone()
        binding.selectFolderTitle.gone()
        binding.selectAllFolderBtn.gone()

        binding.allSelected.visible()
        binding.selectAllTitle.visible()
        binding.selectAllBtn.visible()
    }


    override fun onDeleteClicked(list: List<Audio>) {
        val filePaths = list.map { it.mediaObject?.id ?: "" }
        val uris = mutableListOf<Uri>()
        val audioIds = mutableListOf<Int>()
        for(path in filePaths) {
            val id = path.toIntOrNull() ?: return
            audioIds.add(id)
            uris.add(MusicUtil.getSongFileUri(id))
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val pendingIntent = MediaStore.createDeleteRequest(
                this.contentResolver, uris
            )
            uris.onEach {
                println("onDeleteClicked uris: $it")
            }
            deleteResultLauncher.launch(
                IntentSenderRequest.Builder(pendingIntent.intentSender).build()
            )
        } else {
            audioIds.onEach { id ->
                removeOrRenameFile.deleteAudio(this, id)
                MusicPlayerRemote.checkAfterDeletePlaying()
                removeAudioNotExist(this)
            }

        }
        sendBroadcast(Intent(Utils.ACTION_FINISH_DOWNLOAD))
    }

    override fun onSelectFolder() {
        binding.searchBar.gone()

        binding.allFolderSelected.visible()
        binding.selectFolderTitle.visible()
        binding.selectAllFolderBtn.visible()

        binding.allSelected.gone()
        binding.selectAllTitle.gone()
        binding.selectAllBtn.gone()
    }

    override fun onDeleteFolderClicked(list: List<Folder>) {
        TODO("Not yet implemented")
    }
}