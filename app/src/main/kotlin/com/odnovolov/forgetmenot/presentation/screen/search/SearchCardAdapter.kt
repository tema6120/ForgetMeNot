package com.odnovolov.forgetmenot.presentation.screen.search

import android.text.SpannableStringBuilder
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
import com.odnovolov.forgetmenot.presentation.common.highlightMatches
import com.odnovolov.forgetmenot.presentation.screen.search.SearchEvent.CardClicked
import com.odnovolov.forgetmenot.presentation.screen.search.SearchEvent.CardLongClicked
import kotlinx.android.synthetic.main.item_card_overview.view.*

class SearchCardAdapter(
    private val controller: SearchController
) : ListAdapter<SelectableSearchCard, SimpleRecyclerViewHolder>(DiffCallback()) {
    init {
        stateRestorationPolicy = PREVENT_WHEN_EMPTY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_overview, parent, false)
        return SimpleRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: SimpleRecyclerViewHolder, position: Int) {
        val (cardId: Long, question: String, answer: String, isLearned: Boolean, grade: Int,
            searchText: String, isSelected: Boolean) = getItem(position)
        with(viewHolder.itemView) {
            questionTextView.text = question.highlightMatches(searchText, context)
            questionTextView.isEnabled = !isLearned
            SpannableStringBuilder(answer)
            answerTextView.text = answer.highlightMatches(searchText, context)
            answerTextView.isEnabled = !isLearned
            val gradeColorRes = getGradeColorRes(grade)
            gradeIcon.backgroundTintList = ContextCompat.getColorStateList(context, gradeColorRes)
            gradeIcon.text = grade.toString()
            checkIcon.isVisible = isSelected
            cardView.isSelected = isSelected
            cardView.setOnClickListener {
                controller.dispatch(CardClicked(cardId))
            }
            cardView.setOnLongClickListener {
                controller.dispatch(CardLongClicked(cardId))
                true
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SelectableSearchCard>() {
        override fun areItemsTheSame(
            oldItem: SelectableSearchCard,
            newItem: SelectableSearchCard
        ): Boolean {
            return oldItem.cardId == newItem.cardId
        }

        override fun areContentsTheSame(
            oldItem: SelectableSearchCard,
            newItem: SelectableSearchCard
        ): Boolean {
            return oldItem == newItem
        }
    }
}