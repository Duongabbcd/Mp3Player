package com.example.musicplayer.screen.home.adapter

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ItemSongBinding
import com.example.musicplayer.screen.detail.DetailPlaylistActivity
import com.example.musicplayer.screen.detail.bottomsheet.PlaylistSongBottomSheet
import com.example.musicplayer.screen.home.bottomsheet.SongBottomSheet
import com.example.musicplayer.utils.Common.gone
import com.example.musicplayer.utils.Common.visible
import com.example.musicplayer.utils.Constant
import com.example.musicplayer.utils.Utils
import com.example.service.model.Audio
import com.example.service.service.MusicPlayerRemote

class SongAdapter( private val isPlaylist: Boolean = false, private val onSelectListener: (Audio) -> Unit)  :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items: MutableList<Audio> = mutableListOf()
    private var selectedItems: MutableList<Audio> = mutableListOf()
    private var keys = ""
    private var idPlaylist = 0
    private lateinit var context: Context

    private var isShowSelection = false

    var lastVisibleItemPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        val itemSongBinding =
            ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(itemSongBinding)
    }

    fun updateData(list: List<Audio>) {
        println("updateData: $list")
        lastVisibleItemPosition = 9999
        items.clear()
        items.addAll(list)

        notifyDataSetChanged()
    }


    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
       if(holder is SongViewHolder) {
           holder.bind(position)
       }
    }

    fun getSearchInput(input: String?) {
        keys = input ?: ""
    }

    fun displaySelection(input: Boolean = false) {
        isShowSelection = input
        println("displaySelection 2: $isShowSelection")
        notifyDataSetChanged()
    }

    fun displayAllSelected(input: List<Audio>) {
        selectedItems.clear()
        selectedItems.addAll(input)
        notifyDataSetChanged()
    }

    inner class SongViewHolder(private val binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val song =  items[position]
            println("SongViewHolder: $song")
            binding.apply {
                val string =
                    song.mediaObject?.path?.let { it.substring(it.lastIndexOf('/') + 1, it.length) }
                val songTitle = song.mediaObject?.title
                val songArt =
                    Utils.getAlbumArt(song.mediaObject?.path ?: "")
                println("currentAudio: $songArt")

                if(selectedItems.contains(song)) {
                    selectIcon.setImageResource(R.drawable.checkbox_sorting_select)
                } else {
                    selectIcon.setImageResource(R.drawable.checkbox_un_select)
                }

                if(isShowSelection) {
                    selectIcon.visible()

                    root.setOnClickListener {
                        onSelectListener(song)
                    }
                    selectIcon.setOnClickListener {
                        onSelectListener(song)
                    }
                    moreIcon.gone()
                    favourite.gone()
                } else {
                    selectIcon.gone()
                    root.setOnClickListener {
                        MusicPlayerRemote.openQueue(ArrayList(items), position, true)
                        notifyItemChanged(position)
                    }
                    moreIcon.visible()
                    favourite.visible()
                }

                if (songArt != null) {
                    binding.songAvatar.setImageBitmap(songArt)
                } else {
                    binding.songAvatar.setImageResource(R.drawable.icon_avt_song)
                }

                binding.songArtist.text =
                    if (song.artistName.isEmpty() || !song.artistName[0].isLetter() || song.artistName.contains(
                            Constant.UNKNOWN_STRING,
                            true
                        )
                    ) context.resources.getString(R.string.unknown_artist) else song.artistName

                val title = if (songTitle.isNullOrEmpty()) string else songTitle

                val spannable = SpannableString(title)
                val startIndex = title?.indexOf(keys, ignoreCase = true) ?: 0
                if (startIndex >= 0) {
                    spannable.setSpan(
                        ForegroundColorSpan(
                            context.getColor(R.color.high_light)
                        ),
                        startIndex, startIndex + keys.trim().length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                binding.songName.text = spannable

                moreIcon.setOnClickListener {

                    if(isPlaylist) {
                        val bottomSheet = PlaylistSongBottomSheet(context)
                        bottomSheet.setAudio(song, DetailPlaylistActivity.idPlaylist)
                        bottomSheet.show()
                    } else {
                        val bottomSheet = SongBottomSheet(context)
                        bottomSheet.setAudio(song)
                        bottomSheet.show()
                    }

                }


            }
        }
    }

}