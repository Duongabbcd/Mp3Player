package com.example.musicplayer.screen.detail.bottomsheet

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.base.BaseBottomSheetDialog
import com.example.musicplayer.databinding.BottomSheetEditPlaylistBinding
import com.example.musicplayer.screen.playlist.PhotoActivity
import com.example.musicplayer.utils.Common.gone
import com.example.musicplayer.utils.Common.visible
import com.example.service.model.Playlist

class EditPlaylistBottomSheet(private val context: Context, private val onUpdateInfoListener: (Boolean, Uri, String) -> Unit) :
    BaseBottomSheetDialog<BottomSheetEditPlaylistBinding>(context) {
    private var currentPlaylist: Playlist? = null

    private var editedName = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.closeButton.setOnClickListener {
            dismiss()
        }
    }
    fun updatePlaylist(playlist: Playlist) {
        println("updatePlaylist: $playlist")

        currentPlaylist = playlist
        binding.apply {
            if(imageThumb != Uri.EMPTY  || playlist.thumbUri != "") {
                currentPhoto.visible()
                defaultPhoto.gone()
                if(imageThumb == Uri.EMPTY) {
                    Glide.with(context).load(playlist.thumbUri).placeholder(R.drawable.icon_avt_song).error(R.drawable.icon_avt_song).into(currentPhoto)
                } else {
                    Glide.with(context).load(imageThumb).placeholder(R.drawable.icon_avt_song).error(R.drawable.icon_avt_song).into(currentPhoto)

                }

            } else {
                currentPhoto.gone()
                defaultPhoto.visible()
            }
            editTextName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    editedName = s.toString()
                    if(editedName.isEmpty()) {
                        createBtn.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.unselect_button))
                        createBtn.setTextColor(context.getColor(R.color.unselect_sort))
                    } else {
                        createBtn.setTextColor(context.getColor(R.color.white))
                        createBtn.backgroundTintList = ColorStateList.valueOf(context.getColor(R.color.button_language))
                    }
                }
            })

            playlistAvatar.setOnClickListener {
                context.startActivity(Intent(context, PhotoActivity::class.java))
            }

            changeAvatar.setOnClickListener {
                context.startActivity(Intent(context, PhotoActivity::class.java))
            }

            cancelBtn.setOnClickListener {
             onUpdateInfoListener(false,  imageThumb, editedName)
                dismiss()
            }
            createBtn.setOnClickListener {
                onUpdateInfoListener(true,  imageThumb, editedName)
                dismiss()
            }


        }

    }

    override fun getViewBinding(): BottomSheetEditPlaylistBinding {
        return BottomSheetEditPlaylistBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        setContentView(binding.root)
    }

    companion object {
        var imageThumb : Uri = Uri.EMPTY
    }
}