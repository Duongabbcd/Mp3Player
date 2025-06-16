package com.example.musicplayer.screen.search

import android.os.Bundle
import androidx.activity.viewModels
import com.example.musicplayer.base.BaseActivity
import com.example.musicplayer.databinding.ActivitySearchBinding
import com.example.musicplayer.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchActivity : BaseActivity<ActivitySearchBinding>(ActivitySearchBinding::inflate) {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {

        }
    }
}