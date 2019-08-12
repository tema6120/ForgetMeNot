package com.odnovolov.forgetmenot.ui.pronunciation

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.toFlagEmoji
import com.odnovolov.forgetmenot.ui.pronunciation.LanguageRecyclerAdapter.ViewHolder
import com.odnovolov.forgetmenot.ui.pronunciation.PronunciationViewModel.State.DropdownLanguage
import kotlinx.android.synthetic.main.item_language.view.*
import java.util.*

class LanguageRecyclerAdapter(
    private val onItemClick: (language: Locale?) -> Unit
) : ListAdapter<DropdownLanguage, ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_language, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.apply {
            val dropdownLanguage: DropdownLanguage = getItem(position)
            if (dropdownLanguage.locale == null) {
                languageNameTextView.text = "Default"
                flagTextView.text = null
            } else {
                languageNameTextView.text = dropdownLanguage.locale.displayLanguage
                flagTextView.text = dropdownLanguage.locale.toFlagEmoji()
            }
            if (dropdownLanguage.isSelected) {
                languageItemButton.setBackgroundColor(Color.GRAY)
            } else {
                languageItemButton.background = null
            }
            languageItemButton.setOnClickListener {
                onItemClick(dropdownLanguage.locale)
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class DiffCallback : DiffUtil.ItemCallback<DropdownLanguage>() {
        override fun areItemsTheSame(oldItem: DropdownLanguage, newItem: DropdownLanguage): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: DropdownLanguage, newItem: DropdownLanguage): Boolean {
            return oldItem == newItem
        }
    }

}