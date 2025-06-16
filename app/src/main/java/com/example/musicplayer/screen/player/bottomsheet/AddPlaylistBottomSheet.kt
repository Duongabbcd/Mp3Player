package com.example.musicplayer.screen.player.bottomsheet

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.base.BaseBottomSheetDialog
import com.example.musicplayer.databinding.BottomSheetAddPlaylistBinding
import com.example.musicplayer.databinding.ItemSelectPlaylistBinding
import com.example.musicplayer.screen.playlist.PlaylistActivity
import com.example.musicplayer.screen.playlist.bottomsheet.PlaylistBottomSheet
import com.example.service.model.Audio
import com.example.service.model.Playlist
import com.example.service.utils.MDManager
import com.example.service.utils.PlaylistUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddPlaylistBottomSheet (private val context: Context) :
    BaseBottomSheetDialog<BottomSheetAddPlaylistBinding>(context) {
        private val addPlaylistAdapter: AddPlaylistAdapter by lazy {
            AddPlaylistAdapter {
                CoroutineScope(Dispatchers.IO).launch {
                    val playlist = PlaylistUtils.getInstance(context)?.getPlaylistById(it)
                    println("BottomSheetAddPlaylist: $it $playlist")
                    var x = true
                    if (dataList.isEmpty() || (audio == null || audio == Audio.EMPTY_SONG)) {
                        return@launch
                    }

                    if (dataList.isEmpty()) {
                        x = PlaylistUtils.getInstance(context)?.addSongToPlaylist(audio, playlist) ?: false
                    } else {
                        PlaylistUtils.getInstance(context)
                            ?.addListSongToPlaylistOne(dataList, playlist)
                    }
                    withContext(Dispatchers.Main) {
                        if(x) {
                            MDManager.getInstance(context)?.showMessage(
                                context, context.getString(R.string.add_song_success)
                            )
                        } else {
                            MDManager.getInstance(context)?.showMessage(
                                context, context.getString(R.string.song_added_before)
                            )
                        }
//                        context.sendBroadcast(Intent(PlaylistActivity.ACTION_UPDATE_DATA_PLAYLIST))
                        dismiss()
                    }

                }
            }
        }

    private val allPlaylist = mutableListOf<Playlist>()
    private var audio: Audio? = null
    private var dataList: MutableList<Audio> = mutableListOf()

    override fun getViewBinding(): BottomSheetAddPlaylistBinding {
        return BottomSheetAddPlaylistBinding.inflate(layoutInflater)
    }

    fun setData(inputData: Audio? = null, listAudio: List<Audio> = mutableListOf()) {
        audio = inputData
        dataList.clear()
        dataList.addAll(listAudio)
    }


    override fun initViews() {
        setContentView(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.allPlaylists.adapter = addPlaylistAdapter
        CoroutineScope(Dispatchers.IO).launch{
            val playlists = PlaylistUtils.getInstance(context)?.getPlaylists() ?: listOf()

            println("onCreate: $playlists")
            allPlaylist.clear()
            allPlaylist.addAll(playlists)
            withContext(Dispatchers.Main) {
                addPlaylistAdapter.submitList(allPlaylist)
            }
        }

        binding.apply {
            newPlaylist.setOnClickListener {
                audio?.let {
                    val dialogName = PlaylistBottomSheet(context)
                    dialogName.setData(it, dataList)
                    dialogName.setOnReloadData {
                        context.sendBroadcast(Intent(PlaylistActivity.ACTION_UPDATE_DATA_PLAYLIST))
                    }
                    dialogName.show()
                    dismiss()
                }

            }

        }
    }

}


class AddPlaylistAdapter(private val onClickItem: (Int) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val allPlaylists = mutableListOf<Playlist>()
    private lateinit var  context: Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        val itemSelectPlaylistBinding =
            ItemSelectPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddPlaylistViewHolder(itemSelectPlaylistBinding)
    }

    override fun getItemCount(): Int = allPlaylists.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is AddPlaylistViewHolder) {
            holder.bind(position)
        }
    }

    fun submitList(input: List<Playlist>) {
        allPlaylists.clear()
        allPlaylists.addAll(input)
        notifyDataSetChanged()
    }

    inner class AddPlaylistViewHolder(private val binding: ItemSelectPlaylistBinding) :  RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val playlist = allPlaylists[position]
            println("AddPlaylistViewHolder: $playlist")
            binding.apply {
                playlistName.text = playlist.title

                val size = playlist.tracks.size
                val title = if(size <= 1) {
                    context.resources.getString(R.string.one_song)
                } else {
                    context.resources.getString(R.string.many_songs)
                }
                playlistSongCount.text = "($size) $title"

                binding.root.setOnClickListener {
                    val currentPlaylistId = playlist.id ?: -1
                    onClickItem(currentPlaylistId)
                }

                binding.options.setOnClickListener {
                    val currentPlaylistId = playlist.id ?: -1
                    onClickItem(currentPlaylistId)
                }
            }
        }
    }

}