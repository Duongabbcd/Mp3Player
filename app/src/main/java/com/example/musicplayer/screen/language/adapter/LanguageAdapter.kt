package com.example.musicplayer.screen.language.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.databinding.RcvLanguageBinding

class LanguageAdapter(private val onClickListener: OnClickListener) :
    RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {
        private lateinit var context: Context

    private var selected = -1
    private var languageList: ArrayList<Language> = ArrayList()


    class ViewHolder(val binding: RcvLanguageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            RcvLanguageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    interface OnClickListener {
        fun onClickListener(position: Int, language: Language)
    }

    fun updateData(data: ArrayList<Language>) {
        languageList.clear()
        languageList.addAll(data)
        notifyDataSetChanged()
    }

    fun updatePosition(position: Int) {
        selected = position
        notifyDataSetChanged()
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val language = languageList[position]
        context = holder.binding.root.context
        if (selected == position) {
            holder.binding.language.setBackgroundResource(R.drawable.btn_choselanguage)
            holder.binding.languageName.setTextColor(context.getColor(R.color.selected_language))
            holder.binding.checkbox.setImageResource(R.drawable.checkbox_select)
        } else {
            holder.binding.language.setBackgroundResource(R.drawable.btn_language_no_select)
            holder.binding.languageName.setTextColor(context.getColor(R.color.white))
            holder.binding.checkbox.setImageResource(R.drawable.checkbox_un_select)
        }

        holder.binding.languageIcon.setImageResource(language.img)
        holder.binding.languageName.text = language.name
        holder.binding.languageName.isSelected = true

        holder.itemView.setOnClickListener {
            onClickListener.onClickListener(position,language)
        }
    }

    override fun getItemCount(): Int {
        return languageList.size
    }
}

data class Language(val img: Int, val name: String, val key: String)
