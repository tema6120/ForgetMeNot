package com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.cards

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardPrototype
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder
import kotlinx.android.synthetic.main.item_card_prototype.view.*

class CardPrototypeAdapter(
    private val onCardClicked: (id: Long) -> Unit
) : ListAdapter<CardPrototype, SimpleRecyclerViewHolder>(DiffCallback()) {
    init {
        stateRestorationPolicy = PREVENT_WHEN_EMPTY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_prototype, parent, false)
        return SimpleRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: SimpleRecyclerViewHolder, position: Int) {
        val cardPrototype: CardPrototype = getItem(position)
        with(viewHolder.itemView) {
            questionTextView.text = cardPrototype.question
            answerTextView.text = cardPrototype.answer
            questionTextView.isEnabled = cardPrototype.isSelected
            answerTextView.isEnabled = cardPrototype.isSelected
            checkIcon.isVisible = cardPrototype.isSelected
            cardView.setOnClickListener { onCardClicked(cardPrototype.id) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CardPrototype>() {
        override fun areItemsTheSame(oldItem: CardPrototype, newItem: CardPrototype): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: CardPrototype, newItem: CardPrototype): Boolean =
            oldItem == newItem
    }
}