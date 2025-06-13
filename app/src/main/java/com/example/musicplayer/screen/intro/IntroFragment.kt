package com.example.musicplayer.screen.intro

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ViewpagerIntroItempageBinding
import com.statussaver.video.photo.dowloader.ads.AdsManager
import com.statussaver.video.photo.dowloader.ads.RemoteConfig

class IntroFragment : Fragment() {
    private var bgId = 0
    private var dot = 0
    private var size = 4
    private var position = 0
    private var text = ""
    private var content = ""
    private val binding by lazy { ViewpagerIntroItempageBinding.inflate(layoutInflater) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {
        return binding.root
    }

    private var callbackIntro: CallbackIntro2? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isAdded && activity == null) {
            return
        }
        activity?.let { activity ->
            if (activity is CallbackIntro2) callbackIntro = activity
            bgId = arguments?.getInt("id") ?: R.drawable.bg_intro1
            dot = arguments?.getInt("dot") ?: R.drawable.icon_intro_1
            size = arguments?.getInt("size") ?: 4
            position = arguments?.getInt("position") ?: 0
            text = arguments?.getString("text") ?: getString(R.string.intro_1)
            content = arguments?.getString("content") ?: ""
            binding.image.setImageResource(bgId)
            binding.title.text = text
            binding.slideDot.setImageResource(dot)


            binding.nextBtn.setOnClickListener {
                callbackIntro?.onNext(position + 1)
            }

            Log.d("TAG======", "onViewCreated: $position")
            Log.d("TAG======", "onViewCreated: ${RemoteConfig.native_intro_160425}")
            if(position == 2 && RemoteConfig.native_intro_160425 != "0") {
                AdsManager.showNativeLanguage(activity,binding.frNative, AdsManager.NATIVE_INTRO_3)
            }

        }
    }

    companion object {

        @JvmStatic
        fun newInstance(
            bgId: Int, text: String, content: String, position: Int, size: Int, dot: Int,
        ): IntroFragment {
            val args = Bundle()
            args.putInt("id", bgId)
            args.putInt("dot", dot)
            args.putInt("size", size)
            args.putInt("position", position)
            args.putString("text", text)
            args.putString("content", content)
            val f = IntroFragment()
            f.arguments = args
            return f
        }
    }
}