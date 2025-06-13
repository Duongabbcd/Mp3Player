package com.example.musicplayer.screen.playlist

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayer.base.BaseActivity
import com.example.musicplayer.databinding.ActivityPhotoBinding
import com.example.musicplayer.databinding.ItemPhotoBinding
import com.example.musicplayer.screen.detail.DetailPlaylistActivity
import com.example.musicplayer.screen.detail.bottomsheet.EditPlaylistBottomSheet
import com.example.musicplayer.utils.Common.gone
import com.example.musicplayer.utils.Common.visible

class PhotoActivity : BaseActivity<ActivityPhotoBinding>(ActivityPhotoBinding::inflate){
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var imageUris: List<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            checkPermissions()
            binding.applyBtn.gone()

            backBtn.setOnClickListener {
                startActivity(Intent(this@PhotoActivity, DetailPlaylistActivity::class.java).apply {
                    putExtra("Photo", true)
                })
                finish()
            }

            applyBtn.setOnClickListener {
                startActivity(Intent(this@PhotoActivity, DetailPlaylistActivity::class.java).apply {
                    putExtra("Photo", true)
                })
                finish()
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                loadImages()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun checkPermissions() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // For Android 13 and above
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // For Android 10 to 12
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            else -> {
                // For Android 9 and below
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun loadImages() {
        imageUris = getAllPhotos()
        imageAdapter = ImageAdapter(this, imageUris) { uri ->
            binding.applyBtn.visible()
            EditPlaylistBottomSheet.imageThumb = uri
        }
        binding.allPhotos.adapter = imageAdapter
        binding.allPhotos.layoutManager = GridLayoutManager(this, 3)
    }

    private fun getAllPhotos(): List<Uri> {
        val imageList = mutableListOf<Uri>()
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val cursor = contentResolver.query(collection, projection, null, null, sortOrder)
        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(collection, id)
                imageList.add(contentUri)
            }
        }
        return imageList
    }

    override fun onBackPressed() {
        startActivity(Intent(this@PhotoActivity, DetailPlaylistActivity::class.java).apply {
            putExtra("Photo", true)
        })
        finish()
    }
}

class ImageAdapter(private val context: Context, private val images: List<Uri>, private val onSelectPhotoListener : (Uri) -> Unit) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    inner class ImageViewHolder(private val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root) {
       fun bind(position: Int) {
           val photo = images[position]
           Glide.with(context).load(photo).into(binding.iconDrag)

           binding.root.setOnClickListener {
               onSelectPhotoListener(photo)
           }
       }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val itemSongBinding =
            ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(itemSongBinding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(position)

    }

    override fun getItemCount(): Int = images.size

}