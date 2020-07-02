package com.odnovolov.forgetmenot.presentation.screen.deckcontent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder
import kotlinx.android.synthetic.main.item_card_overview.view.*

class CardOverviewAdapter : ListAdapter<Card, SimpleRecyclerViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_overview, parent, false)
        return SimpleRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: SimpleRecyclerViewHolder, position: Int) {
        val item: Card = getItem(position)
        with(viewHolder.itemView) {
            questionTextView.text = item.question
            answerTextView.text = item.answer
            cardView.setOnClickListener {
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Card>() {
        override fun areItemsTheSame(oldItem: Card, newItem: Card): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Card, newItem: Card): Boolean =
            oldItem == newItem
    }
}