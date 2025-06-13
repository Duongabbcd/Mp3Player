package com.example.musicplayer.utils

import com.example.musicplayer.R
import com.example.musicplayer.screen.language.adapter.Language

object GlobalConstant {
    fun getListLocation(): ArrayList<Language> {
        val listLanguage: ArrayList<Language> = ArrayList<Language>()
        listLanguage.add(Language(R.drawable.english, "English", "en"))
        listLanguage.add(Language(R.drawable.filipino, "Filipino", "fil"))
        listLanguage.add(Language(R.drawable.portuguese, "Portuguese", "pt"))
        listLanguage.add(Language(R.drawable.indonesian, "Indonesian", "in"))
        listLanguage.add(Language(R.drawable.hindi, "Hindi", "hi"))
        listLanguage.add(Language(R.drawable.afrikaans, "Afrikaans", "af"))
        listLanguage.add(Language(R.drawable.afrikaans, "Xhosa", "xh"))
        listLanguage.add(Language(R.drawable.afrikaans, "Zulu", "zu"))
        listLanguage.add(Language(R.drawable.afrikaans, "Venda", "ve"))
        listLanguage.add(Language(R.drawable.afrikaans, "Tsonga", "ts"))
        listLanguage.add(Language(R.drawable.spanish, "Spanish", "es"))
        listLanguage.add(Language(R.drawable.arabic, "Arabic", "ar"))
        listLanguage.add(Language(R.drawable.uzbek, "Uzbek", "uz"))
        listLanguage.add(Language(R.drawable.german, "German", "de"))
        listLanguage.add(Language(R.drawable.french, "French", "fr"))
        listLanguage.add(Language(R.drawable.kazakh,"Kazakh", "kk"))
        listLanguage.add(Language(R.drawable.malaysia, "Malay", "ms"))
        return listLanguage
    }
}