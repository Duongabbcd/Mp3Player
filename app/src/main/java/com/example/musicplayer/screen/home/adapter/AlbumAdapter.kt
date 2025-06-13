package com.example.musicplayer.screen.home.adapter

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ItemAlbumBinding
import com.example.service.model.Album

class AlbumAdapter(private val onClickListener: (Album) -> Unit) :  RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items: ArrayList<Album> = arrayListOf()
    private var keys = ""
    private lateinit var context: Context

    var lastVisibleItemPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        val itemFolderBinding =
            ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FolderViewHolder(itemFolderBinding)
    }

    fun updateData(list: List<Album>) {
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
        if(holder is FolderViewHolder) {
            holder.bind(position)
        }
    }

    inner class FolderViewHolder(private val binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val album =  items[position]
            val title = album.albumName
            binding.apply {
                val text = if(album.numberSong.toInt() <= 1) {
                    context.resources.getString(R.string.one_song)
                } else {
                    context.resources.getString(R.string.many_songs)
                }
                albumSize.text = album.numberSong.plus(" $text")

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

                albumName.text = spannable

                root.setOnClickListener {
                    onClickListener(album)
                }
            }
        }
    }
}