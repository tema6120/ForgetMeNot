package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import com.odnovolov.forgetmenot.R.layout
import com.odnovolov.forgetmenot.R.string
import com.odnovolov.forgetmenot.presentation.common.toFlagEmoji
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.LanguageAdapter.ViewHolder
import kotlinx.android.synthetic.main.item_language.view.*
import java.util.*

class LanguageAdapter(
    private val onItemClick: (language: Locale?) -> Unit
) : ListAdapter<DropdownLanguage, ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(layout.item_language, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.apply {
            val dropdownLanguage: DropdownLanguage = getItem(position)
            if (dropdownLanguage.language == null) {
                languageNameTextView.text = context.getString(string.default_name)
                flagTextView.text = null
            } else {
                languageNameTextView.text = dropdownLanguage.language.displayLanguage
                flagTextView.text = dropdownLanguage.language.toFlagEmoji()
            }
            isSelected = dropdownLanguage.isSelected
            languageItemButton.setOnClickListener {
                onItemClick(dropdownLanguage.language)
            }
        }
    }

    class ViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view)

    class DiffCallback : ItemCallback<DropdownLanguage>() {
        override fun areItemsTheSame(
            oldItem: DropdownLanguage,
            newItem: DropdownLanguage
        ): Boolean {
            return oldItem.language == newItem.language
        }

        override fun areContentsTheSame(
            oldItem: DropdownLanguage,
            newItem: DropdownLanguage
        ): Boolean {
            return oldItem == newItem
        }
    }
}