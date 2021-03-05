package com.odnovolov.forgetmenot.presentation.screen.search

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder
import com.odnovolov.forgetmenot.presentation.common.getGradeColorRes
import com.odnovolov.forgetmenot.presentation.common.highlight
import com.odnovolov.forgetmenot.presentation.screen.search.SearchEvent.CardClicked
import com.odnovolov.forgetmenot.presentation.screen.search.SearchEvent.CardLongClicked
import kotlinx.android.synthetic.main.item_card_overview.view.*

class SearchCardAdapter(
    private val controller: SearchController
) : RecyclerView.Adapter<SimpleRecyclerViewHolder>() {
    var items: List<SelectableSearchCard> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_overview, parent, false)
        return SimpleRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: SimpleRecyclerViewHolder, position: Int) {
        val selectableSearchCard = items[position]
        val card = selectableSearchCard.card
        with(viewHolder.itemView) {
            questionTextView.text = card.question
                .highlight(selectableSearchCard.questionMatchingRanges, context)
            questionTextView.isEnabled = !card.isLearned
            SpannableStringBuilder(card.answer)
            answerTextView.text = card.answer
                .highlight(selectableSearchCard.answerMatchingRanges, context)
            answerTextView.isEnabled = !card.isLearned
            val gradeColorRes = getGradeColorRes(card.grade)
            gradeIcon.backgroundTintList = ContextCompat.getColorStateList(context, gradeColorRes)
            gradeIcon.text = card.grade.toString()
            checkIcon.isVisible = selectableSearchCard.isSelected
            cardView.isSelected = selectableSearchCard.isSelected
            cardView.setOnClickListener {
                controller.dispatch(CardClicked(selectableSearchCard))
            }
            cardView.setOnLongClickListener {
                controller.dispatch(CardLongClicked(selectableSearchCard))
                true
            }
        }
    }

    override fun getItemCount(): Int = items.size
}