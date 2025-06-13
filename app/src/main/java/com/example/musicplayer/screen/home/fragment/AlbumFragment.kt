package com.example.musicplayer.screen.home.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.BaseFragment
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentAlbumBinding
import com.example.musicplayer.screen.album.AlbumActivity
import com.example.musicplayer.screen.artist.ArtistActivity
import com.example.musicplayer.screen.home.adapter.AlbumAdapter
import com.example.musicplayer.screen.home.adapter.ArtistAdapter
import com.example.musicplayer.utils.Common.gone
import com.example.musicplayer.utils.Common.visible
import com.example.musicplayer.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.checkerframework.checker.units.qual.A

@AndroidEntryPoint
class AlbumFragment : BaseFragment<FragmentAlbumBinding>(FragmentAlbumBinding::inflate){
    private val albumAdapter = AlbumAdapter(onClickListener = {
        withSafeContext { ctx ->
            val intent = Intent(ctx, AlbumActivity::class.java)
            AlbumActivity.album = it
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    })

    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            allAlbums.adapter = albumAdapter
            withSafeContext { ctx ->
                allAlbums.layoutManager = LinearLayoutManager(ctx)
            }


            viewModel.allAlbums.observe(viewLifecycleOwner) { items ->
                emptyLayout.gone()
                allAlbums.visible()
                if(items.isEmpty()) {
                    emptyLayout.visible()
                    allAlbums.gone()
                    return@observe
                }

                val size = items.size
                val text = if(size <= 1) {
                    "(0) ".plus(resources.getString(R.string.one_album))
                } else {
                    "($size) ".plus(resources.getString(R.string.many_albums))
                }
                binding.albumSize.text = text


                albumAdapter.updateData(items)
            }

            viewModel.searchInput.observe(viewLifecycleOwner) { input ->
                albumAdapter.getSearchInput(input)
            }

            // For RecyclerView scroll
            allAlbums.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        fun newInstance() = AlbumFragment().apply { }
    }
}