package com.example.musicplayer.screen.home.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ItemPlaylistBinding
import com.example.musicplayer.screen.detail.DetailPlaylistActivity
import com.example.musicplayer.utils.Common.gone
import com.example.musicplayer.utils.Common.visible
import com.example.service.model.Playlist

class PlaylistAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items: ArrayList<Playlist> = arrayListOf()
    private var keys = ""
    private lateinit var context: Context

    var lastVisibleItemPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        val itemPlaylistBinding =
            ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistViewHolder(itemPlaylistBinding)
    }

    fun updateData(list: List<Playlist>) {
        lastVisibleItemPosition = 9999
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun getSearchInput(input: String?) {
        keys = input ?: ""
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is PlaylistViewHolder) {
            holder.bind(position)
        }
    }

    inner class PlaylistViewHolder(private val binding: ItemPlaylistBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(position: Int) {
            val playlist =  items[position]
            val title =playlist.title
            binding.apply {
                val text = if(playlist.tracks.size <=1) {
                    context.resources.getString(R.string.one_song)
                } else {
                    context.resources.getString(R.string.many_songs)
                }

                if(playlist.thumbUri != "") {
                    customLayout.visible()
                    defaultLayout.gone()
                    Glide.with(context).load(Uri.parse(playlist.thumbUri)).placeholder(R.drawable.icon_avt_song).error(R.drawable.icon_avt_song).into(
                        binding.cardView
                    )
                } else {
                    customLayout.gone()
                    defaultLayout.visible()
                }

                binding.playlistSize.text = playlist.tracks.size.toString().plus(" $text")

                val spannable = SpannableString(title)
                val startIndex = title.indexOf(keys, ignoreCase = true)
                if (startIndex >= 0) {
                    spannable.setSpan(
                        ForegroundColorSpan(
                            context.getColor(R.color.white)
                        ),
                        startIndex, startIndex + keys.trim().length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                playlistName.text = spannable

                root.setOnClickListener {
                    val intent = Intent(context, DetailPlaylistActivity::class.java )
                    DetailPlaylistActivity.idPlaylist = playlist.id ?: -1
                    context.startActivity(intent)
                }
            }
        }
    }
}