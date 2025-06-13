package com.example.musicplayer.screen.language

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.admob.max.dktlibrary.utils.admod.NativeHolderAdmob
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ActivityLanguageBinding
import com.example.musicplayer.screen.home.MainActivity
import com.example.musicplayer.screen.intro.IntroActivity
import com.example.musicplayer.screen.language.adapter.Language
import com.example.musicplayer.screen.language.adapter.LanguageAdapter
import com.example.musicplayer.screen.setting.SettingActivity
import com.example.musicplayer.utils.Common
import com.example.musicplayer.utils.Common.gone
import com.example.musicplayer.utils.Common.visible
import com.example.musicplayer.utils.GlobalConstant
import com.statussaver.video.photo.dowloader.ads.AdsManager
import com.statussaver.video.photo.dowloader.ads.RemoteConfig
import com.example.musicplayer.base.BaseActivity

class LanguageActivity : BaseActivity<ActivityLanguageBinding>(ActivityLanguageBinding::inflate){
    private var flag = R.drawable.english
    private var adapter2: LanguageAdapter? = null
    private var start = false
    private var selectedLanguage = ""
    private var selectedLanguageName = ""

    private val allLanguages = GlobalConstant.getListLocation()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        start = intent.getBooleanExtra("fromSplash", false)
        binding.apply {
            if (start) {
                backBtn.gone()
                showAds()
                selectedLanguage = ""


                if(RemoteConfig.native_intro_160425 != "0") {
                    AdsManager.loadNative(this@LanguageActivity, AdsManager.NATIVE_INTRO_3)
                }

            } else {
                backBtn.visible()
                selectedLanguage = Common.getPreLanguage(this@LanguageActivity)
                selectedLanguageName = Common.getLang(this@LanguageActivity)
                binding.backBtn.setOnClickListener {
                    val intent = Intent(this@LanguageActivity, SettingActivity::class.java)
                    startActivity(intent.apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    })
                }
                binding.applyBtn.setBackgroundResource(R.drawable.background_language_select)
                binding.applyBtn.setTextAppearance(R.style.CustomTextStyleMedium12sp)
                binding.applyBtn.setTextColor(resources.getColor(R.color.white))

            }
        }
        getLanguage()
        changeLanguageDone()
    }

    private fun changeLanguageDone() {
        binding.applyBtn.setOnClickListener {
            println("changeLanguageDone: $selectedLanguage and $selectedLanguageName")
            if (selectedLanguage != "") {
                Common.setPreLanguage(this@LanguageActivity, selectedLanguage.split("-").first())
                binding.applyBtn.isEnabled = false
                binding.applyBtn.isFocusable = false
                if(!start) {
                    startActivity(Intent(this, SettingActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    })
                    finish()
                } else {
                    startActivity(Intent(this, IntroActivity::class.java))
                    finish()
                }
            }else {
                Toast.makeText(this, "Please select language", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getLanguage() {
        adapter2 = LanguageAdapter(object: LanguageAdapter.OnClickListener {
            override fun onClickListener(position: Int, language: Language) {
                selectedLanguage = language.key
                selectedLanguageName = language.name

                adapter2?.updatePosition(position)
            }

        })
        adapter2?.updateData(allLanguages)
        binding.allLanguages.layoutManager = LinearLayoutManager(this)
        binding.allLanguages.adapter = adapter2
//        binding.rcvLanguageList.setHasFixedSize(true)
    }


    private fun showAds(nativeHolderAdmob: NativeHolderAdmob = AdsManager.NATIVE_LANGUAGE) {
        try {
            when(RemoteConfig.native_language_160425) {
                "1" -> {
                    AdsManager.showNativeLanguage(this, binding.frNative , nativeHolderAdmob)
                }
                else -> {
                    binding.frNative.gone()
                }
            }
        }catch (_: Exception){
            binding.frNative.gone()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }
}