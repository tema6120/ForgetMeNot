package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder
import com.odnovolov.forgetmenot.presentation.common.setTintFromRes
import com.odnovolov.forgetmenot.presentation.common.toFlagEmoji
import kotlinx.android.synthetic.main.item_language.view.*
import java.util.*

class LanguageAdapter(
    private val onItemClicked: (language: Locale?) -> Unit,
    private val onMarkLanguageAsFavoriteButtonClicked: (language: Locale) -> Unit,
    private val onUnmarkLanguageAsFavoriteButtonClicked: (language: Locale) -> Unit,
) : RecyclerView.Adapter<SimpleRecyclerViewHolder>() {
    var items: List<DisplayedLanguage> = emptyList()
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_language, parent, false)
        return SimpleRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: SimpleRecyclerViewHolder, position: Int) {
        val displayedLanguage: DisplayedLanguage = items[position]
        with(viewHolder.itemView) {
            if (displayedLanguage.language == null) {
                languageNameTextView.text = context.getString(R.string.default_language)
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
            with(favoriteLanguageButton) {
                when (displayedLanguage.isFavorite) {
                    null -> isVisible = false
                    true -> {
                        isVisible = true
                        setImageResource(R.drawable.ic_round_star_24)
                        setTintFromRes(R.color.favorite_language_checked)
                        setOnClickListener {
                            onUnmarkLanguageAsFavoriteButtonClicked(displayedLanguage.language!!)
                        }
                    }
                    false -> {
                        isVisible = true
                        setImageResource(R.drawable.ic_round_star_border_24)
                        setTintFromRes(R.color.favorite_language_unchecked)
                        setOnClickListener {
                            onMarkLanguageAsFavoriteButtonClicked(displayedLanguage.language!!)
                        }
                    }
                }
            }
            languageItemButton.isSelected = displayedLanguage.isSelected
            languageItemButton.setOnClickListener {
                onItemClicked(displayedLanguage.language)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}