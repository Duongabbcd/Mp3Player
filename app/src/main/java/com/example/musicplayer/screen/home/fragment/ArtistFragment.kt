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
import com.example.musicplayer.databinding.FragmentArtistBinding
import com.example.musicplayer.screen.artist.ArtistActivity
import com.example.musicplayer.screen.home.adapter.AlbumAdapter
import com.example.musicplayer.screen.home.adapter.ArtistAdapter
import com.example.musicplayer.utils.Common.gone
import com.example.musicplayer.utils.Common.visible
import com.example.musicplayer.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ArtistFragment : BaseFragment<FragmentArtistBinding>(FragmentArtistBinding::inflate){
    private val artistAdapter = ArtistAdapter(onClickListener = {
        withSafeContext { ctx ->
            val intent = Intent(ctx, ArtistActivity::class.java)
            ArtistActivity.artist = it
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    })


    private val viewModel: MainViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            allArtists.adapter = artistAdapter
            withSafeContext { ctx ->
                allArtists.layoutManager = LinearLayoutManager(ctx)
            }


            viewModel.allArtists.observe(viewLifecycleOwner) { items ->
                emptyLayout.gone()
                allArtists.visible()
                if(items.isEmpty()) {
                    emptyLayout.visible()
                    allArtists.gone()
                    return@observe
                }

                val size = items.size
                val text = if(size <= 1) {
                    "(0) ".plus(resources.getString(R.string.one_artist))
                } else {
                    "($size) ".plus(resources.getString(R.string.many_artists))
                }
                binding.artistSize.text = text

                artistAdapter.updateData(items)
            }

            viewModel.searchInput.observe(viewLifecycleOwner) { input ->
                artistAdapter.getSearchInput(input)
            }

            // For RecyclerView scroll
            allArtists.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        fun newInstance() = ArtistFragment().apply { }
    }
}