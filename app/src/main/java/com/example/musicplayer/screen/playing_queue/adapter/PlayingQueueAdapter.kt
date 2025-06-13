package com.example.musicplayer.screen.playing_queue.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ItemQueueSongBinding
import com.example.musicplayer.screen.home.bottomsheet.SongBottomSheet
import com.example.musicplayer.utils.Constant
import com.example.musicplayer.utils.Utils
import com.example.service.model.Audio

class PlayingQueueAdapter  :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items: MutableList<Any> = mutableListOf()
    private lateinit var context: Context

    var lastVisibleItemPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        val itemSongBinding =
            ItemQueueSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QueueSongViewHolder(itemSongBinding)
    }

    fun updateData(list: List<Any>) {
        println("updateData: $list")
        lastVisibleItemPosition = 9999
        items.clear()
        items.addAll(list)

        notifyDataSetChanged()
    }


    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is QueueSongViewHolder) {
            holder.bind(position)
        }
    }


    inner class QueueSongViewHolder(private val binding: ItemQueueSongBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val song =  items[position] as Audio

            binding.apply {
                val songTitle = song.mediaObject?.title
                val songArt =
                    Utils.getAlbumArt(song.mediaObject?.path ?: "")
                println("currentAudio: $songArt")

                songName.text = songTitle

                if (songArt != null) {
                    binding.songAvatar.setImageBitmap(songArt)
                } else {
                    binding.songAvatar.setImageResource(R.drawable.icon_avt_song)
                }

                binding.numberSong.text =
                    if (song.artistName.isEmpty() || !song.artistName[0].isLetter() || song.artistName.contains(
                            Constant.UNKNOWN_STRING,
                            true
                        )
                    ) context.resources.getString(R.string.unknown_artist) else song.artistName


                iconDrag.setOnClickListener {
                    val bottomSheet = SongBottomSheet(context)
                    bottomSheet.setAudio(song)
                    bottomSheet.show()
                }


            }
        }
    }

}