package com.example.musicplayer.screen.player.bottomsheet

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.cheonjaeung.powerwheelpicker.android.SampleItemEffector
import com.cheonjaeung.powerwheelpicker.android.sample.TimePickerAdapter
import com.example.musicplayer.R
import com.example.musicplayer.base.BaseBottomSheetDialog
import com.example.musicplayer.databinding.BottomSheetTimeSleepBinding
import com.example.musicplayer.utils.Common
import com.example.musicplayer.utils.Utils
import com.example.service.service.MusicPlayerRemote

@SuppressLint("ClickableViewAccessibility")
class TimeSleepBottomSheet (private val context: Context) :
    BaseBottomSheetDialog<BottomSheetTimeSleepBinding>(context) {

    private lateinit var hourAdapter: TimePickerAdapter
    private lateinit var minuteAdapter: TimePickerAdapter

    override fun getViewBinding(): BottomSheetTimeSleepBinding {
        return BottomSheetTimeSleepBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        setContentView(binding.root)
    }

    private var hour = 0
    private var minute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            hourAdapter = TimePickerAdapter(0, 23)
            minuteAdapter = TimePickerAdapter(0, 59)

            hourWheelPicker.adapter = hourAdapter
            minuteWheelPicker.adapter = minuteAdapter

            behavior.isDraggable = false
            root.setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        behavior.isDraggable = true
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        behavior.isDraggable = false
                    }
                }
                false
            }

            hourWheelPicker.addItemEffector(
                SampleItemEffector(
                    hourWheelPicker,
                    ContextCompat.getColor(context,R.color.white),
                    ContextCompat.getColor(context,R.color.gray),  36f, 1f
                )
            )
            minuteWheelPicker.addItemEffector(
                SampleItemEffector(
                    minuteWheelPicker,
                    ContextCompat.getColor(context,R.color.white),
                    ContextCompat.getColor(context,R.color.gray), 36f, 1f
                )
            )

            textOk.setOnClickListener {
                val currentHour = hourWheelPicker.currentPosition
                val currentMinute  = minuteWheelPicker.currentPosition

                if(currentHour > 0) {
                    hour = currentHour
                }
                if(currentMinute > 0) {
                    minute = currentMinute
                }
                println("TimeSleepBottomSheet: $hour and $minute")
                val totalTime = hour * 3600000L  + minute * 60000L

                if(hour == 0 && minute == 0) {
                    Toast.makeText(context, "Time must be greater than 0", Toast.LENGTH_SHORT).show()
                } else {
                    Common.setCountDownTime(context, totalTime)
                    MusicPlayerRemote.startTimer(totalTime)
                }
                this@TimeSleepBottomSheet.dismiss()
            }

            textCancel.setOnClickListener {
                dismiss()
            }

            button15m.setOnClickListener {
                hour = 0
                minute = 15
                Utils.setBackgroundColor(listOf(button15m), listOf(button30m, button45m, button60m),
                    context.resources.getColor(R.color.select_button),
                    context.resources.getColor(R.color.bottom_sheet_content),
                )
            }

            button30m.setOnClickListener {
                hour = 0
                minute = 30
                Utils.setBackgroundColor(listOf(button30m), listOf(button15m, button45m, button60m),
                    context.resources.getColor(R.color.select_button),
                    context.resources.getColor(R.color.bottom_sheet_content),
                )
            }

            button45m.setOnClickListener {
                hour = 0
                minute = 45
                Utils.setBackgroundColor(listOf(button45m), listOf(button30m, button15m, button60m),
                    context.resources.getColor(R.color.select_button),
                    context.resources.getColor(R.color.bottom_sheet_content),
                )
            }

            button60m.setOnClickListener {
                hour = 0
                minute = 60
                Utils.setBackgroundColor(listOf(button60m), listOf(button30m, button45m, button15m),
                    context.resources.getColor(R.color.select_button),
                    context.resources.getColor(R.color.bottom_sheet_content),
                )
            }

        }
    }


}