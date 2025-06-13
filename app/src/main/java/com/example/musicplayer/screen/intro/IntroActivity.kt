package com.example.musicplayer.screen.intro

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivityIntroBinding
import com.example.musicplayer.screen.home.MainActivity
import com.example.musicplayer.screen.intro.adapter.IntroAdapter
import com.statussaver.video.photo.dowloader.ads.AdsManager
import com.statussaver.video.photo.dowloader.ads.RemoteConfig
import com.example.musicplayer.base.BaseActivity

class IntroActivity : BaseActivity<ActivityIntroBinding>(ActivityIntroBinding::inflate), CallbackIntro2{
    private var isBackground = false
    private var isShowInter = true


    private var image2 = mutableListOf<Int>()
    private var content2 = mutableListOf<String>()
    private var dots = mutableListOf<Int>()
    private var titles = mutableListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        isShowInter = true
        intro()
    }

    private fun intro() {

        postToList()

        binding.viewpager.adapter = IntroAdapter(this, image2, content2, dots, titles)
    }

    private fun nextScreen() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        })
        finish()
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (binding.viewpager.currentItem != 0) {
            binding.viewpager.currentItem -= 1
        } else {
            super.onBackPressed()
        }
    }

    private fun addToList2(imageV: Int, textV: String, title: String, dot: Int) {
        image2.add(imageV)
        content2.add(textV)
        dots.add(dot)
        titles.add(title)
    }

    private fun postToList() {
        addToList2(
            R.drawable.bg_intro1,
            "",
            getString(R.string.intro_1),
            R.drawable.icon_intro_1
        )
        addToList2(
            R.drawable.bg_intro2,
            "",
            getString(R.string.intro_2),
            R.drawable.icon_intro_2
        )
        addToList2(
            R.drawable.bg_intro3,
            "",
            getString(R.string.intro_3),
            R.drawable.icon_intro_3
        )

    }


    override fun onStop() {
        super.onStop()
        isBackground = true
    }

    override fun onNext(position: Int) {
        println("onNext: $position")
        if (position == image2.size) {
            nextScreen()
        } else {
            binding.viewpager.currentItem += 1
        }
    }

    override fun onSkip(position: Int) {
        println("onSkip: ${RemoteConfig.inter_skip_intro_160425}")
        if(RemoteConfig.inter_skip_intro_160425 != "0") {
            AdsManager.loadAndShowInterSP2(
                this, AdsManager.INTER_SKIP_INTRO, "INTER_SKIP_INTRO",
                object : AdsManager.AdListenerWithNative {
                    override fun onAdClosedOrFailed() {
                        println("onAdClosedOrFailed")
                        nextScreen()
                    }

                    override fun onAdClosedOrFailedWithNative() {
                        println("onAdClosedOrFailedWithNative")
                        nextScreen()
                    }

                }
            )
        } else {
            nextScreen()
        }
    }

}


interface CallbackIntro2 {
    fun onNext(position: Int)
    fun onSkip(position: Int)
}
