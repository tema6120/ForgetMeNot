package com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder
import com.odnovolov.forgetmenot.presentation.common.getGradeColorRes
import com.odnovolov.forgetmenot.presentation.common.setTooltipTextFromContentDescription
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent.DeckContentEvent.*
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent.ItemInDeckContentList.SelectableCard
import kotlinx.android.synthetic.main.item_card_overview.view.*
import kotlinx.android.synthetic.main.toolbar_deck_content.view.*

class CardOverviewAdapter(
    private val controller: DeckContentController
) : ListAdapter<ItemInDeckContentList, SimpleRecyclerViewHolder>(DiffCallback()) {
    init {
        stateRestorationPolicy = PREVENT_WHEN_EMPTY
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0)
            R.layout.toolbar_deck_content else
            R.layout.item_card_overview
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        if (viewType == R.layout.toolbar_deck_content) {
            view.exportButton.run {
                setOnClickListener { controller.dispatch(ExportButtonClicked) }
                setTooltipTextFromContentDescription()
            }
            view.searchButton.run {
                setOnClickListener { controller.dispatch(SearchButtonClicked) }
                setTooltipTextFromContentDescription()
            }
        }
        return SimpleRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: SimpleRecyclerViewHolder, position: Int) {
        val item: ItemInDeckContentList = getItem(position)
        if (item == ItemInDeckContentList.Header) return
        val selectableCard: SelectableCard = item as SelectableCard
        val card = selectableCard.card
        if (position == 0) return
        with(viewHolder.itemView) {
            questionTextView.text = card.question
            questionTextView.isEnabled = !card.isLearned
            answerTextView.text = card.answer
            answerTextView.isEnabled = !card.isLearned
            val gradeColorRes = getGradeColorRes(card.grade)
            gradeIcon.backgroundTintList = ContextCompat.getColorStateList(context, gradeColorRes)
            gradeIcon.text = card.grade.toString()
            checkIcon.isVisible = selectableCard.isSelected
            cardView.isSelected = selectableCard.isSelected
            cardView.setOnClickListener {
                controller.dispatch(CardClicked(card.id))
            }
            cardView.setOnLongClickListener {
                controller.dispatch(CardLongClicked(card.id))
                true
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ItemInDeckContentList>() {
        override fun areItemsTheSame(
            oldItem: ItemInDeckContentList,
            newItem: ItemInDeckContentList
        ): Boolean {
            return when {
                oldItem === newItem -> true
                oldItem is SelectableCard && newItem is SelectableCard -> {
                    oldItem.card.id == newItem.card.id
                }
                else -> false
            }
        }

        override fun areContentsTheSame(
            oldItem: ItemInDeckContentList,
            newItem: ItemInDeckContentList
        ): Boolean {
            return oldItem == newItem
        }
    }
}