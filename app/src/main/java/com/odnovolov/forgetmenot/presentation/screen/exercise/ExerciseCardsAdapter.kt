package com.odnovolov.forgetmenot.presentation.screen.exercise

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseCard
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseCardsAdapter.ViewHolder
import kotlinx.android.synthetic.main.exercise_card_content.view.*

class ExerciseCardsAdapter : ListAdapter<ExerciseCard, ViewHolder>(DiffCallback()) {

    var showAnswerButtonClickLister: ((idx: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        getItem(position)?.let { exerciseCard: ExerciseCard ->
            viewHolder.itemView.apply {
                questionTextView.text = exerciseCard.card.question
                answerTextView.text = exerciseCard.card.answer
                if (exerciseCard.isAnswered) {
                    answerTextView.visibility = View.VISIBLE
                    showAnswerButton.visibility = View.INVISIBLE
                } else {
                    answerTextView.visibility = View.INVISIBLE
                    showAnswerButton.visibility = View.VISIBLE
                }
                showAnswerButton.setOnClickListener { showAnswerButtonClickLister?.invoke(exerciseCard.id) }
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class DiffCallback : DiffUtil.ItemCallback<ExerciseCard>() {
        override fun areItemsTheSame(oldItem: ExerciseCard, newItem: ExerciseCard): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ExerciseCard, newItem: ExerciseCard): Boolean {
            return oldItem == newItem
        }
    }
}