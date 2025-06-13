package com.example.musicplayer.screen.setting

import android.content.Intent
import android.os.Bundle

import com.example.musicplayer.databinding.ActivitySettingBinding
import com.example.musicplayer.screen.home.MainActivity
import com.example.musicplayer.screen.language.LanguageActivity
import com.example.musicplayer.base.BaseActivity

class SettingActivity : BaseActivity<ActivitySettingBinding>(ActivitySettingBinding::inflate) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            language.setOnClickListener {
                startActivity(Intent(this@SettingActivity, LanguageActivity::class.java))
            }

            backBtn.setOnClickListener {
                startActivity(Intent(this@SettingActivity, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                })
            }
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this@SettingActivity, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
    }
}