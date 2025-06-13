package com.example.musicplayer.screen.intro.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.musicplayer.screen.intro.IntroFragment

class IntroAdapter(
    fragmentActivity: FragmentActivity, private val image: List<Int>,
    val content: List<String>, private val dots : List<Int>, val title : List<String>
) :
    FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return IntroFragment.newInstance(
            image[position],
            title[position],
            content[position],
            position,image.size,dots[position]
        )
    }

    override fun getItemCount(): Int {
        return image.size
    }
}
