package com.example.musicplayer.screen.home.adapter

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ItemFolderBinding
import com.example.musicplayer.databinding.ItemSongBinding
import com.example.musicplayer.utils.Common.gone
import com.example.musicplayer.utils.Common.visible
import com.example.musicplayer.utils.Constant
import com.example.musicplayer.utils.Utils
import com.example.service.model.Audio
import com.example.service.model.Folder

class FolderAdapter(private val onClickListener : (Folder) -> Unit)  :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var items: ArrayList<Folder> = arrayListOf()
    private var keys = ""
    private lateinit var context: Context
    private var isShowSelection = false
    private var selectedItems: MutableList<Folder> = mutableListOf()
    var lastVisibleItemPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        val itemFolderBinding =
            ItemFolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FolderViewHolder(itemFolderBinding)
    }

    fun updateData(list: List<Folder>, text: String = "") {
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

    fun displaySelection(input: Boolean = false) {
        isShowSelection = input
        println("displaySelection 2: $isShowSelection")
        notifyDataSetChanged()
    }

    fun displayAllSelected(input: List<Folder>) {
        selectedItems.clear()
        selectedItems.addAll(input)
        notifyDataSetChanged()
    }

    inner class FolderViewHolder(private val binding: ItemFolderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val folder =  items[position]
            val title =  folder.name
            binding.apply {
                folderSize.text = "(${folder.tracks.size})"

                if(isShowSelection) {
                    root.setOnClickListener { onClickListener(folder) }
                    selectButton.visible()

                } else {
                    selectButton.gone()

                }

                if(selectedItems.contains(folder)) {
                    selectBackground.visible()
                    selectButton.setImageResource(R.drawable.checkbox_sorting_select)
                } else {
                    selectBackground.gone()
                    selectButton.setImageResource(R.drawable.checkbox_un_select)
                }

                val spannable = SpannableString(title)
                val startIndex = title.indexOf(keys, ignoreCase = true) ?: 0
                if (startIndex >= 0) {
                    spannable.setSpan(
                        ForegroundColorSpan(
                            context.getColor(R.color.high_light)
                        ),
                        startIndex, startIndex + keys.trim().length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }

                folderName.text = spannable
            }
        }
    }
}