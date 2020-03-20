package com.odnovolov.forgetmenot.presentation.screen.repetition.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R.layout
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionCard
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.RepetitionCardAdapter.ViewHolder
import kotlinx.android.synthetic.main.item_repetition_card.view.*

class RepetitionCardAdapter(
    val controller: RepetitionViewController
) : RecyclerView.Adapter<ViewHolder>() {
    var items: List<RepetitionCard> = emptyList()
        set(value) {
            if (value != field) {
                field = value
            }
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(layout.item_repetition_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val repetitionCard: RepetitionCard = items[position]
        with(viewHolder.itemView) {
            questionTextView.text = if (repetitionCard.isReverse) {
                repetitionCard.card.answer
            } else {
                repetitionCard.card.question
            }
            answerTextView.text = if (repetitionCard.isReverse) {
                repetitionCard.card.question
            } else {
                repetitionCard.card.answer
            }
            if (repetitionCard.isAnswered) {
                answerScrollView.visibility = View.VISIBLE
                showAnswerButton.visibility = View.GONE
                showAnswerButton.setOnClickListener(null)
            } else {
                answerScrollView.visibility = View.GONE
                showAnswerButton.visibility = View.VISIBLE
                showAnswerButton.setOnClickListener {
                    controller.onShowAnswerButtonClicked()
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}