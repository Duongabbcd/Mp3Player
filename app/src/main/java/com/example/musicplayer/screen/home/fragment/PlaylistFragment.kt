package com.example.musicplayer.screen.home.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.BaseFragment
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentPlaylistBinding
import com.example.musicplayer.screen.home.adapter.PlaylistAdapter
import com.example.musicplayer.screen.playlist.bottomsheet.PlaylistBottomSheet
import com.example.musicplayer.utils.Common.gone
import com.example.musicplayer.utils.Common.visible
import com.example.musicplayer.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlaylistFragment : BaseFragment<FragmentPlaylistBinding>(FragmentPlaylistBinding::inflate){
    private lateinit var playlistAdapter: PlaylistAdapter
    private val viewModel: MainViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            playlistAdapter = PlaylistAdapter()
            allPlaylists.adapter = playlistAdapter
            withSafeContext { ctx ->
                allPlaylists.layoutManager = LinearLayoutManager(ctx)
            }
            createNewPlaylist.setOnClickListener {
                withSafeContext { ctx ->
                    val dialogName = PlaylistBottomSheet(ctx)
                    dialogName.setOnReloadData {
                        viewModel.getAllPlaylists()
                    }
                    dialogName.show()
                }
            }

            newPlaylist.setOnClickListener {
                withSafeContext { ctx ->
                    println("context is null: false")
                    val dialogName = PlaylistBottomSheet(ctx)
                    dialogName.setOnReloadData {
                        viewModel.getAllPlaylists()
                    }
                    dialogName.show()
                }
            }

            viewModel.allPlaylists.observe(viewLifecycleOwner) { items ->
                println("newPlaylist: $items")
                val size = items.size
                val text = if(size <= 1) {
                    "(0) ".plus(resources.getString(R.string.one_playlist))
                } else {
                    "($size) ".plus(resources.getString(R.string.many_playlists))
                }
                binding.playlistSize.text = text

                if(items.size <= 1) {
                    emptyLayout.visible()
                    allPlaylists.gone()
                    createNewPlaylist.gone()
                    return@observe
                }
                emptyLayout.gone()
                allPlaylists.visible()
                createNewPlaylist.visible()
                playlistAdapter.updateData(items)
            }

            viewModel.searchInput.observe(viewLifecycleOwner) { input ->
                playlistAdapter.getSearchInput(input)
            }

            // For RecyclerView scroll
            allPlaylists.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = PlaylistFragment().apply { }
    }
}