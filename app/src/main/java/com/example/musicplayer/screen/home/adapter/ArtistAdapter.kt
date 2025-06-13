package com.example.musicplayer.screen.home.adapter

import android.content.Context
import android.content.Intent
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ItemArtistBinding
import com.example.service.model.Artist

class ArtistAdapter(private val onClickListener : (Artist) -> Unit):  RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items: ArrayList<Artist> = arrayListOf()
    private var keys = ""
    private lateinit var context: Context

    var lastVisibleItemPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        val itemFolderBinding =
            ItemArtistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArtistViewHolder(itemFolderBinding)
    }

    fun updateData(list: List<Artist>) {
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
        if(holder is ArtistViewHolder) {
            holder.bind(position)
        }
    }

    inner class ArtistViewHolder(private val binding: ItemArtistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val artist =  items[position]
            val title = artist.nameArtist
            binding.apply {
                val text = if(artist.numberSong.toInt() <= 1) {
                    context.resources.getString(R.string.one_song)
                } else {
                    context.resources.getString(R.string.many_songs)
                }
                artistSongCount.text = artist.numberSong.plus(" $text")


                val spannable = SpannableString(title)
                val startIndex = title.indexOf(keys, ignoreCase = true) ?: 0
                if (startIndex >= 0) {
                    spannable.setSpan(
                        ForegroundColorSpan(
                            context.getColor(R.color.white)
                        ),
                        startIndex, startIndex + keys.trim().length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                artistName.text = spannable

                root.setOnClickListener {
                    onClickListener(artist)
                }
            }
        }
    }
}