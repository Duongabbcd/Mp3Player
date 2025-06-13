package com.example.musicplayer.screen.player.bottomsheet

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.example.musicplayer.base.BaseBottomSheetDialog
import com.example.musicplayer.databinding.BottomSheetCountDownBinding
import com.example.musicplayer.utils.Common
import com.example.service.service.MusicPlayerRemote
import com.example.service.service.MusicService

class CountDownBottomSheet  (private val context: Context) :
    BaseBottomSheetDialog<BottomSheetCountDownBinding>(context) {
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == MusicService.ACTION_UPDATE_SLEEP_TIMER) {
               updateUI()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        val totalTime = MusicPlayerRemote.timeLeft()
        val hour = totalTime / 1000 /60
        val minute = totalTime / 1000 % 60

        binding.apply {
            hourValue.text = hour.toString()
            minuteValue.text = minute.toString()
        }
    }

    override fun getViewBinding(): BottomSheetCountDownBinding {
       val binding = BottomSheetCountDownBinding.inflate(layoutInflater)
        return binding
    }

    override fun initViews() {
        setContentView(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            val totalTime = MusicPlayerRemote.timeLeft()
            val hour = totalTime / 1000 /60
            val minute = totalTime / 1000 % 60
            hourValue.text = hour.toString()
            minuteValue.text = minute.toString()


            endButton.setOnClickListener {
                MusicPlayerRemote.cancelTimer()
                dismiss()
            }

            resetButton.setOnClickListener {
                val initialValue = Common.getCountDownTime(context)
                MusicPlayerRemote.startTimer(initialValue)
                dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(MusicService.ACTION_UPDATE_SLEEP_TIMER)
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )

    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        context.unregisterReceiver(receiver)
    }

}