package com.example.musicplayer.screen.splash

import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.example.musicplayer.BaseActivity2
import com.example.musicplayer.databinding.ActivitySplashBinding
import com.example.musicplayer.screen.language.LanguageActivity
import com.example.musicplayer.utils.Common

class SplashActivity : BaseActivity2<ActivitySplashBinding>(ActivitySplashBinding::inflate) {
    private var handler = Handler(Looper.getMainLooper())
    private var runnable = Runnable {
//        if (showAds){
//            initAdmob()
//        }
    }

    private var showAds = true
    override fun initView() {
        Common.setPreLanguage(this, "en")
        handler.postDelayed(runnable,20000)

        nextScreen()
    }

    private fun nextScreen() {
        val intent = Intent(this@SplashActivity, LanguageActivity::class.java)
        intent.putExtra("fromSplash", true)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }
}