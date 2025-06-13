package com.example.musicplayer.screen.home.bottomsheet

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.ImageView
import com.example.musicplayer.R
import com.example.musicplayer.base.BaseBottomSheetDialog
import com.example.musicplayer.databinding.BottomSheetSortingBinding
import com.example.musicplayer.utils.Constant
import com.example.service.utils.PreferenceUtil

class SortBottomSheet(private val context: Context, private val isPlaylist: Boolean = false, private val onClickListener: (String) -> Unit) :
    BaseBottomSheetDialog<BottomSheetSortingBinding>(context) {

    private val sortOrder =
        if (isPlaylist) {
            PreferenceUtil.getInstance(context)?.getSongPlaylistSortOrder()
                ?: Constant.ODER_SORT_FILE_DEFAULT
        } else {
            PreferenceUtil.getInstance(context)?.getSongSortOrder()
                ?: Constant.ODER_SORT_FILE_DEFAULT
        }

    private var currentOrder = ""


    override fun getViewBinding(): BottomSheetSortingBinding {
        return BottomSheetSortingBinding.inflate(layoutInflater)
    }


    override fun initViews() {
        setContentView(binding.root)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            when (sortOrder) {
                Constant.ODER_SORT_TITLE_ASC -> firstOption()
                Constant.ODER_SORT_TITLE_DESC -> secondOption()
                Constant.ODER_SORT_FILE_SIZE_DESC -> thirdOption()
                Constant.ODER_SORT_FILE_SIZE_ASC -> forthOption()
                Constant.ODER_SORT_DATE_ADD_ASC -> fifthOption()
                Constant.ODER_SORT_DATE_ADD_DESC -> sixthOption()
            }

            fromAtoZ.setOnClickListener {
                currentOrder = Constant.ODER_SORT_TITLE_ASC
                firstOption()
                checkOption(currentOrder)
            }
            fromZtoA.setOnClickListener {
                currentOrder = Constant.ODER_SORT_TITLE_DESC
                checkOption(currentOrder)
                secondOption()
            }
            fromHighToLow.setOnClickListener {
                currentOrder = Constant.ODER_SORT_FILE_SIZE_DESC
                checkOption(currentOrder)
                thirdOption()
            }
            fromLowToHigh.setOnClickListener {
                currentOrder = Constant.ODER_SORT_FILE_SIZE_ASC
                checkOption(currentOrder)
                forthOption()
            }
            fromNewToOld.setOnClickListener {
                currentOrder = Constant.ODER_SORT_DATE_ADD_ASC
                checkOption(currentOrder)
                fifthOption()
            }
            fromOldToNew.setOnClickListener {
                currentOrder = Constant.ODER_SORT_DATE_ADD_DESC
                checkOption(currentOrder)
                sixthOption()
            }

            applyButton.setOnClickListener {
                onClickListener(currentOrder)
                dismiss()
            }

        }
    }

    private fun checkOption(currentOrder: String = "") {
        if (!sortOrder.equals(currentOrder, true)) {
            binding.applyButton.setTextColor(context.getColor(R.color.select_sort))
            binding.applyButton.backgroundTintList =
                ColorStateList.valueOf(context.getColor(R.color.selected_language))
            binding.applyButton.isEnabled = true
        } else {
            binding.applyButton.setTextColor(context.getColor(R.color.unselect_sort))
            binding.applyButton.backgroundTintList =
                ColorStateList.valueOf(context.getColor(R.color.unselect_button))
            binding.applyButton.isEnabled = false
        }
    }

    private fun firstOption() {
        displaySelectedButton(
            binding.firstSortIcon, listOf(
                binding.secondSortIcon,
                binding.thirdSortIcon,
                binding.forthSortIcon,
                binding.fifthSortIcon,
                binding.sixthSortIcon,
            )
        )
    }

    private fun secondOption() {
        displaySelectedButton(
            binding.secondSortIcon, listOf(
                binding.firstSortIcon,
                binding.thirdSortIcon,
                binding.forthSortIcon,
                binding.fifthSortIcon,
                binding.sixthSortIcon,
            )
        )
    }

    private fun thirdOption() {
        displaySelectedButton(
            binding.thirdSortIcon, listOf(
                binding.secondSortIcon,
                binding.firstSortIcon,
                binding.forthSortIcon,
                binding.fifthSortIcon,
                binding.sixthSortIcon,
            )
        )
    }

    private fun forthOption() {
        displaySelectedButton(
            binding.forthSortIcon, listOf(
                binding.secondSortIcon,
                binding.thirdSortIcon,
                binding.firstSortIcon,
                binding.fifthSortIcon,
                binding.sixthSortIcon,
            )
        )
    }

    private fun fifthOption() {
        displaySelectedButton(
            binding.fifthSortIcon, listOf(
                binding.secondSortIcon,
                binding.thirdSortIcon,
                binding.forthSortIcon,
                binding.firstSortIcon,
                binding.sixthSortIcon,
            )
        )
    }

    private fun sixthOption() {
        displaySelectedButton(
            binding.sixthSortIcon, listOf(
                binding.secondSortIcon,
                binding.thirdSortIcon,
                binding.forthSortIcon,
                binding.fifthSortIcon,
                binding.firstSortIcon,
            )
        )
    }

    private fun displaySelectedButton(selected: ImageView, unselected: List<ImageView>) {
        selected.setImageResource(R.drawable.checkbox_sorting_select)
        unselected.onEach {
            it.setImageResource(R.drawable.checkbox_un_select)
        }
    }
}