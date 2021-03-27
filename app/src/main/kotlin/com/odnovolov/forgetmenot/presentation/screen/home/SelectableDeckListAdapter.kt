package com.odnovolov.forgetmenot.presentation.screen.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder
import kotlinx.android.synthetic.main.item_deck_list.view.*

class SelectableDeckListAdapter(
    private val onDeckListButtonClicked: (deckListId: Long?) -> Unit
) : RecyclerView.Adapter<SimpleRecyclerViewHolder>() {
    var items: List<SelectableDeckList> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_deck_list, parent, false)
        return SimpleRecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: SimpleRecyclerViewHolder, position: Int) {
        val selectableDeckList: SelectableDeckList = items[position]
        with(viewHolder.itemView) {
            val iconDrawable = DeckListDrawableGenerator.generateIcon(selectableDeckList.color)
            deckListIcon.setImageDrawable(iconDrawable)
            deckListNameTextView.text = selectableDeckList.name
                ?: context.getString(R.string.deck_list_title_all_decks)
            numberOfDecksTextView.text = selectableDeckList.size.toString()
            deckListButton.background =
                if (selectableDeckList.isSelected) {
                    DeckListDrawableGenerator
                        .generateBackgroundForSelectedItem(selectableDeckList.color, context)
                } else {
                    null
                }
            deckListButton.setOnClickListener {
                onDeckListButtonClicked(selectableDeckList.id)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}