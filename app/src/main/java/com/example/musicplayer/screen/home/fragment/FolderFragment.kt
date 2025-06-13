package com.example.musicplayer.screen.home.fragment

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.BaseFragment
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentFolderBinding
import com.example.musicplayer.screen.home.MainActivity.Companion.displayMode
import com.example.musicplayer.screen.home.adapter.FolderAdapter
import com.example.musicplayer.screen.player.bottomsheet.AddPlaylistBottomSheet
import com.example.musicplayer.utils.Common.gone
import com.example.musicplayer.utils.Common.visible
import com.example.musicplayer.viewmodel.MainViewModel
import com.example.service.model.Audio
import com.example.service.model.Folder
import com.example.service.service.MusicPlayerRemote
import dagger.hilt.android.AndroidEntryPoint
import java.util.ArrayList

@AndroidEntryPoint
class FolderFragment : BaseFragment<FragmentFolderBinding>(FragmentFolderBinding::inflate){
    private val folderAdapter: FolderAdapter by lazy {
        FolderAdapter { item ->
          viewModel.getSelectedFolders(item)
        }
    }
    private val viewModel: MainViewModel by activityViewModels()
    private var callback: OnSelectFolderListener? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

            allFolders.adapter = folderAdapter
            withSafeContext { ctx ->
                allFolders.layoutManager = GridLayoutManager(ctx, 2)
            }


            viewModel.allFolder.observe(viewLifecycleOwner) { items ->
                emptyLayout.gone()
                allFolders.visible()
                if(items.isEmpty()) {
                    emptyLayout.visible()
                    allFolders.gone()
                    return@observe
                }

                val size = items.size
                val text = if(size <= 1) {
                    "(0) ".plus(resources.getString(R.string.one_folder))
                } else {
                    "($size) ".plus(resources.getString(R.string.many_folders))
                }
                binding.folderSize.text = text

                folderAdapter.updateData(items, "")
            }

            viewModel.searchInput.observe(viewLifecycleOwner) { input ->
                folderAdapter.getSearchInput(input)
            }

            // For RecyclerView scroll
            allFolders.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                        viewModel.checkKeyboard(true)
                    }
                }
            })


            viewModel.isLoading.observe(viewLifecycleOwner) { input ->
                if(input) {
                    progressBar.visible()
                } else {
                    progressBar.gone()
                }
            }

            selectButton.setOnClickListener {
                callback?.onSelectFolder()
                viewModel.checkSelection(true)
            }

            viewModel.isShowSelection.observe(viewLifecycleOwner) { input ->
                println("displaySelection 1: $input")
                folderAdapter.displaySelection(input)
                if (input) {
                    searchBar.visible()
                    fixedFunctionLayout.visible()
                    allFunctions.gone()
                } else {
                    searchBar.gone()
                    fixedFunctionLayout.gone()
                    allFunctions.visible()
                }
            }

            viewModel.allSelectedFolder.observe(viewLifecycleOwner) { items ->
                folderAdapter.displayAllSelected(items)
                val allTracks = items.toAudioList()
                if (items.size == 0) {
                    deleteTitle.setTextColor(resources.getColor(R.color.gray))
                    addPlaylistTitle.setTextColor(resources.getColor(R.color.gray))
                    playTitle.setTextColor(resources.getColor(R.color.gray))
                    deleteIcon.setImageResource(R.drawable.icon_gray_delete)
                    addPlaylistIcon.setImageResource(R.drawable.icon_gray_playlist)
                    playIcon.setImageResource(R.drawable.icon_gray_mini_play)

                    deleteButton.isEnabled = false
                    addPlaylist.isEnabled = false
                    playButton.isEnabled = false
                } else {
                    deleteTitle.setTextColor(resources.getColor(R.color.white))
                    addPlaylistTitle.setTextColor(resources.getColor(R.color.white))
                    playTitle.setTextColor(resources.getColor(R.color.white))
                    deleteIcon.setImageResource(R.drawable.icon_white_delete)
                    addPlaylistIcon.setImageResource(R.drawable.icon_white_playlist)
                    playIcon.setImageResource(R.drawable.icon_mini_play)

                    deleteButton.isEnabled = true
                    addPlaylist.isEnabled = true
                    playButton.isEnabled = true

                    playButton.setOnClickListener {
                        allTracks.onEach {
                            println("allTracks: $it")
                        }
                        MusicPlayerRemote.openQueue(ArrayList(allTracks), 0, true)
                    }


                    addPlaylist.setOnClickListener {
                        withSafeContext { ctx ->
                            val dialog = AddPlaylistBottomSheet(ctx)
                            dialog.setData(listAudio = allTracks)
                            dialog.show()
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

    fun List<Folder>.toAudioList(): List<Audio> {
        return this.flatMap { it.tracks }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSelectFolderListener) {
            callback = context
        } else {
            throw ClassCastException("$context must implement OnButtonClickListener")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = FolderFragment().apply { }
    }

    // Define the callback interface
    interface OnSelectFolderListener {
        fun onSelectFolder()
        fun onDeleteFolderClicked(list: List<Folder>)
    }
}