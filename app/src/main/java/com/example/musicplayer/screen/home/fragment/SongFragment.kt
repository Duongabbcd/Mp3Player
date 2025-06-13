package com.example.musicplayer.screen.home.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.BaseFragment
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentSongBinding
import com.example.musicplayer.screen.home.MainActivity.Companion.displayMode
import com.example.musicplayer.screen.home.adapter.SongAdapter
import com.example.musicplayer.screen.home.bottomsheet.SortBottomSheet
import com.example.musicplayer.screen.home.dialog.DeleteSongDialog
import com.example.musicplayer.utils.Common.gone
import com.example.musicplayer.utils.Common.visible
import com.example.musicplayer.viewmodel.MainViewModel
import com.example.service.model.Audio
import com.example.service.service.MusicPlayerRemote
import com.example.service.service.MusicService
import com.example.service.service.MusicService.Companion.SHUFFLE_MODE_SHUFFLE
import com.example.service.utils.PreferenceUtil
import com.example.service.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SongFragment : BaseFragment<FragmentSongBinding>(FragmentSongBinding::inflate) {
    private val songAdapter: SongAdapter by lazy {
        SongAdapter { item ->
            viewModel.getSelectedAudios(item)
        }
    }


    private val viewModel: MainViewModel by activityViewModels()
    private val broadcastChange = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action == Utils.ACTION_FINISH_DOWNLOAD) {
                CoroutineScope(Dispatchers.Main).launch {
                    if (isActive && isAdded) {
                        reloadData(true)
                    }
                }
            }
        }
    }

    override fun reloadData(isDisplayLoading: Boolean) {
        super.reloadData(isDisplayLoading)
        viewModel.getAllSongs()
    }
    private var callback: OnButtonClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnButtonClickListener) {
            callback = context
        } else {
            throw ClassCastException("$context must implement OnButtonClickListener")
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            allSongs.adapter = songAdapter
            withSafeContext { ctx ->
                allSongs.layoutManager = LinearLayoutManager(ctx)
            }

            filter.setOnClickListener {
                withSafeContext { ctx ->
                    val dialog = SortBottomSheet(ctx, false) { string ->
                        PreferenceUtil.getInstance(ctx)?.setSongSortOrder(string)
                        viewModel.getAllSongs()
                    }
                    dialog.show()
                }
            }

            viewModel.allSongs.observe(viewLifecycleOwner) { items ->
                println("allSongs: $items")
                emptyLayout.gone()
                allSongs.visible()
                if (items.isEmpty()) {
                    emptyLayout.visible()
                    allSongs.gone()
                    return@observe
                }
                songAdapter.updateData(items)

                shuffleAll.setOnClickListener {
                    MusicPlayerRemote.openAndShuffleQueue(ArrayList(items), true)
                    MusicPlayerRemote.toggleShuffleMode(SHUFFLE_MODE_SHUFFLE)
                    val result = MusicPlayerRemote.getPlayingQueue() as ArrayList<Audio>
                    viewModel.getAllSongs(result)
                }

                playAll.setOnClickListener {
                    MusicPlayerRemote.openQueue(ArrayList(items), 0, true)
                }

            }

            viewModel.searchInput.observe(viewLifecycleOwner) { input ->
                songAdapter.getSearchInput(input)
            }

            viewModel.isLoading.observe(viewLifecycleOwner) { input ->
                if (input) {
                    progressBar.visible()
                } else {
                    progressBar.gone()
                }
            }

            // For RecyclerView scroll
            allSongs.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                        viewModel.checkKeyboard(true)
                    }
                }
            })

            select.setOnClickListener {
                callback?.onButtonClicked()
                viewModel.checkSelection(true)
            }

            viewModel.isShowSelection.observe(viewLifecycleOwner) { input ->
                println("displaySelection 1: $input")
                songAdapter.displaySelection(input)
                if (input) {
                    searchBar.visible()
                    deleteLayout.visible()
                    allFunctions.gone()
                } else {
                    searchBar.gone()
                    deleteLayout.gone()
                    allFunctions.visible()
                }
            }

            viewModel.allSelectedAudio.observe(viewLifecycleOwner) { items ->
                songAdapter.displayAllSelected(items)
                if (items.size == 0) {
                    deleteButton.setTextColor(resources.getColor(R.color.unselect_sort))
                    deleteButton.backgroundTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.unselect_button))
                    deleteButton.isEnabled = false
                } else {
                    deleteButton.setTextColor(resources.getColor(R.color.select_sort))
                    deleteButton.backgroundTintList =
                        ColorStateList.valueOf(resources.getColor(R.color.selected_language))
                    deleteButton.isEnabled = true
                    deleteButton.setOnClickListener {
                        withSafeContext { ctx ->
                            val deleteDialog = DeleteSongDialog(ctx)
                            deleteDialog.deleteManyAudiosFromDevice(items) { result ->
                                if(result) {
                                    callback?.onDeleteClicked(items)
                                } else {
                                    viewModel.getAllSelectedAudios(listOf())
                                }
                            }
                            deleteDialog.show()
                        }
                    }
                }
            }

            searchText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // Before text is changed
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Text is changing
                }

                override fun afterTextChanged(s: Editable?) {
                    // After text has changed
                    val input = s.toString()
                    viewModel.searchText(input, displayMode)
                }
            })
        }
    }


    override fun onStart() {
        super.onStart()
        val intentFilter = IntentFilter()
        intentFilter.addAction(
            MusicService.PLAY_STATE_CHANGED.replace(
                MusicService.MUSIC_PLAYER_PACKAGE_NAME, MusicService.MUSIC_PACKAGE_NAME
            )
        )
        intentFilter.addAction(Utils.ACTION_FINISH_DOWNLOAD)
        val ctx = context ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ctx.registerReceiver(
                broadcastChange, intentFilter, Context.RECEIVER_EXPORTED
            )
        } else {
            ContextCompat.registerReceiver(
                ctx,
                broadcastChange,
                intentFilter,
                ContextCompat.RECEIVER_EXPORTED
            )

        }

    }

    override fun onStop() {
        super.onStop()
        if (context == null) {
            return
        } else {
            requireContext().unregisterReceiver(broadcastChange)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = SongFragment().apply { }
    }

    // Define the callback interface
    interface OnButtonClickListener {
        fun onButtonClicked()
        fun onDeleteClicked(list: List<Audio>)
    }
}