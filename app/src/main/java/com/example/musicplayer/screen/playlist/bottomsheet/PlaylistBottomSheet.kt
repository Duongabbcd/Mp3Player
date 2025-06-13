package com.example.musicplayer.screen.playlist.bottomsheet

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import com.example.musicplayer.R
import com.example.musicplayer.base.BaseBottomSheetDialog
import com.example.musicplayer.databinding.BottomSheetPlaylistBinding
import com.example.service.model.Audio
import com.example.service.utils.PlaylistUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaylistBottomSheet (private val context: Context) :
    BaseBottomSheetDialog<BottomSheetPlaylistBinding>(context) {
    private var onReload: (String) -> Unit = {}
    private var data: Audio = Audio.EMPTY_SONG
    private val listData = mutableListOf<Audio>()
    private  var song: Audio = Audio.EMPTY_SONG

    override fun getViewBinding(): BottomSheetPlaylistBinding {
        println("currentAudio: $song")
        return BottomSheetPlaylistBinding.inflate(layoutInflater)
    }

    fun setData(audio: Audio = Audio.EMPTY_SONG, dataList: MutableList<Audio> = mutableListOf()) {
        data = audio
        listData.clear()
        listData.addAll(dataList)
    }

    fun setOnReloadData(reload: (String) -> Unit) {
        onReload = reload
    }

    override fun initViews() {
        setContentView(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.editTextName.hint = context.getString(R.string.new_playlist)
        binding.editTextName.requestFocus()

        binding.editTextName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s.toString().isNotEmpty()) {
                    binding.createBtn.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.selected_language))
                    binding.createBtn.setTextColor(context.getColor(R.color.white))
                } else {
                    binding.createBtn.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.unselect_button))
                    binding.createBtn.setTextColor(context.getColor(R.color.unselect_sort))
                }
            }

            override fun afterTextChanged(s: Editable?) {
                //do nothing
            }

        })

        binding.createBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val title = binding.editTextName.text.toString()
                if (title.isEmpty() || title == " ") {
                    binding.editTextName.error = context.getString(R.string.title_must_be_not_empty)
                } else if (PlaylistUtils.getInstance(context)?.checkNameExists(title) == true) {
                    binding.editTextName.error =  context.getString(R.string.title_is_exist)
                } else {
                    withContext(Dispatchers.IO) {
                        if (data != null) {
                            val listData = listOf(data!!)
                            PlaylistUtils.getInstance(context)
                                ?.newPlaylist(title, listData, context)
                        } else {
                            PlaylistUtils.getInstance(context)
                                ?.newPlaylist(title, listData, context)
                        }
                    }
                    onReload(title)
                    dismiss()
                }
            }

        }
        binding.cancelBtn.setOnClickListener {
            dismiss()
        }
    }


}