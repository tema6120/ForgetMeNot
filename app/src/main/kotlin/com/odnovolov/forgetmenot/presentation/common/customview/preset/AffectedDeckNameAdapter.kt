package com.odnovolov.forgetmenot.presentation.common.customview.preset

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder

class AffectedDeckNameAdapter : RecyclerView.Adapter<SimpleRecyclerViewHolder>() {
    var items: List<String> = emptyList()
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_affected_deck_when_preset_is_removed, parent, false)
        return SimpleRecyclerViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: SimpleRecyclerViewHolder, position: Int) {
        val deckName: String = items[position]
        (viewHolder.itemView as TextView).text = "• $deckName"
    }

    override fun getItemCount(): Int = items.size
}