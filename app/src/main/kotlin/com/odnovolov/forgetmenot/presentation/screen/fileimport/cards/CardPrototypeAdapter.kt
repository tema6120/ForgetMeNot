package com.odnovolov.forgetmenot.presentation.screen.fileimport.cards

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder
import kotlinx.android.synthetic.main.item_card_prototype.view.*

class CardPrototypeAdapter(
    private val onCardClicked: (id: Long) -> Unit
) : ListAdapter<CardPrototypeItem, SimpleRecyclerViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_prototype, parent, false)
        return SimpleRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: SimpleRecyclerViewHolder, position: Int) {
        val item: CardPrototypeItem = getItem(position)
        with(viewHolder.itemView) {
            questionTextView.text = item.question
            answerTextView.text = item.answer
            cardView.setOnClickListener { onCardClicked(item.id) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CardPrototypeItem>() {
        override fun areItemsTheSame(
            oldItem: CardPrototypeItem,
            newItem: CardPrototypeItem
        ): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: CardPrototypeItem,
            newItem: CardPrototypeItem
        ): Boolean =
            oldItem == newItem
    }
}

data class CardPrototypeItem(
    val id: Long,
    val question: String,
    val answer: String,
    val isSelected: Boolean
)