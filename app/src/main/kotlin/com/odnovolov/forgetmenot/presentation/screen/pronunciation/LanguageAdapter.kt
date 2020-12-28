package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import com.odnovolov.forgetmenot.R.layout
import com.odnovolov.forgetmenot.R.string
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder
import com.odnovolov.forgetmenot.presentation.common.toFlagEmoji
import kotlinx.android.synthetic.main.item_language.view.*
import java.util.*

class LanguageAdapter(
    private val onItemClick: (language: Locale?) -> Unit
) : ListAdapter<DisplayedLanguage, SimpleRecyclerViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(layout.item_language, parent, false)
        return SimpleRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: SimpleRecyclerViewHolder, position: Int) {
        val displayedLanguage: DisplayedLanguage = getItem(position)
        with(viewHolder.itemView) {
            if (displayedLanguage.language == null) {
                languageNameTextView.text = context.getString(string.default_language)
            } else {
                languageNameTextView.text = displayedLanguage.language.displayLanguage
            }
            val flagEmoji = displayedLanguage.language?.toFlagEmoji()
            if (flagEmoji != null) {
                flagTextView.text = displayedLanguage.language.toFlagEmoji()
                flagTextView.visibility = View.VISIBLE
            } else {
                flagTextView.visibility = View.INVISIBLE
            }
            isSelected = displayedLanguage.isSelected
            languageItemButton.setOnClickListener {
                onItemClick(displayedLanguage.language)
            }
        }
    }

    class DiffCallback : ItemCallback<DisplayedLanguage>() {
        override fun areItemsTheSame(
            oldItem: DisplayedLanguage,
            newItem: DisplayedLanguage
        ): Boolean {
            return oldItem.language == newItem.language
        }

        override fun areContentsTheSame(
            oldItem: DisplayedLanguage,
            newItem: DisplayedLanguage
        ): Boolean {
            return oldItem == newItem
        }
    }
}