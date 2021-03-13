package com.odnovolov.forgetmenot.presentation.screen.decklistseditor

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder

class DeckListColorAdapter(
    private val onItemClicked: (color: Int) -> Unit,
) : RecyclerView.Adapter<SimpleRecyclerViewHolder>() {
    var items: List<SelectableColor> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_deck_list_color, parent, false)
        return SimpleRecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: SimpleRecyclerViewHolder, position: Int) {
        val selectableColor: SelectableColor = items[position]
        with(viewHolder.itemView as ImageButton) {
            isSelected = selectableColor.isSelected
            setColorFilter(selectableColor.color, PorterDuff.Mode.SRC_ATOP)
            setOnClickListener {
                onItemClicked(selectableColor.color)
            }
        }
    }
}