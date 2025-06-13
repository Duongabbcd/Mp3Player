package com.example.musicplayer.screen.player.bottomsheet

import android.content.Context
import android.os.Bundle
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import com.example.musicplayer.R
import com.example.musicplayer.base.BaseBottomSheetDialog
import com.example.musicplayer.databinding.BottomSheetSpeedBinding
import com.example.musicplayer.utils.Common
import com.example.musicplayer.utils.Common.gone
import com.example.musicplayer.utils.Common.visible
import com.example.service.model.Audio
import com.example.service.service.MusicPlayerRemote


class SpeedBottomSheet (private val context: Context, private val onSpeedChangeListener :(Float) -> Unit) :
    BaseBottomSheetDialog<BottomSheetSpeedBinding>(context) {
    private val currentSpeed = MusicPlayerRemote.getSpeed(context)
    override fun getViewBinding(): BottomSheetSpeedBinding {
        return BottomSheetSpeedBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        setContentView(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            speedSeekbar.max = 25
            speedSeekbar.progress = (currentSpeed * 10).toInt()
            displaySpeed.text = currentSpeed.toString()

            println("currentSpeed; $currentSpeed")
            when(currentSpeed) {
               0.5f -> {
                   speedSeekbar.progress = 0
                   displayText(listOf(slowestTitle), listOf(mediumTitle, fastestTitle,custom))
                   displayTick(listOf(slowestTick), listOf(mediumTick, fastestTick))
               }

                1.0f -> {
                    speedSeekbar.progress = 5
                    displayText(listOf(mediumTitle), listOf(slowestTitle, fastestTitle,custom))
                    displayTick(listOf(mediumTick), listOf(slowestTick, fastestTick))
               }

                1.5f -> {
                    speedSeekbar.progress = 10
                    displayText(listOf(fastestTitle), listOf(mediumTitle, slowestTitle, custom))
                    displayTick(listOf(fastestTick), listOf(mediumTick, slowestTick))
               }

                else -> {
                    displayText(listOf(custom), listOf(slowestTitle, mediumTitle, fastestTitle))
                }
            }

            slowest.setOnClickListener {
                displayText(listOf(slowestTitle), listOf(slowestTitle, fastestTitle,custom))
                displayTick(listOf(slowestTick), listOf(slowestTick, fastestTick))
                displaySpeed.text = "0.5"
                speedSeekbar.progress = 0
                onSpeedChangeListener(0.5f)
            }

            medium.setOnClickListener {
                displayText(listOf(mediumTitle), listOf(slowestTitle, fastestTitle,custom))
                displayTick(listOf(mediumTick), listOf(slowestTick, fastestTick))
                displaySpeed.text = "1.0"
                speedSeekbar.progress = 5
                onSpeedChangeListener(1.0f)
            }

            fastest.setOnClickListener {
                displayText(listOf(fastestTitle), listOf(mediumTitle, slowestTitle,custom))
                displayTick(listOf(fastestTick), listOf(mediumTick, slowestTick))
                displaySpeed.text = "1.5"
                speedSeekbar.progress = 10
                onSpeedChangeListener(1.5f)
            }

            speedSeekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val speed = (progress + 5) / 10f
                    displaySpeed.text = String.format("%.1f", speed)
                    if(fromUser) {
                        displayText(listOf(custom), listOf(slowestTitle, mediumTitle, fastestTitle))
                        displayTick(listOf(), listOf(mediumTick, slowestTick, fastestTick))
                        onSpeedChangeListener(speed)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    //do nothing
                }

            })
        }
    }

    private fun displayText(selected: List<TextView>, unselected: List<TextView>) {
        selected.onEach {
            it.setTextColor(context.getColor(R.color.selected_language))
        }
        unselected.onEach {
            it.setTextColor(context.getColor(R.color.white))
        }
    }

    private fun displayTick(selected: List<ImageView>, unselected: List<ImageView>) {
        selected.onEach {
            it.visible()
        }


        unselected.onEach {
            it.gone()
        }
    }



}