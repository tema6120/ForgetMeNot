package com.odnovolov.forgetmenot.presentation.screen.help.article

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.dp
import com.odnovolov.forgetmenot.presentation.common.observe
import com.odnovolov.forgetmenot.presentation.screen.help.article.ExampleExerciseCardAdapter.ExerciseCardViewHolder
import com.odnovolov.forgetmenot.presentation.screen.help.article.ExampleExerciseToDemonstrateCardsRetesting.ExerciseCard
import kotlinx.android.synthetic.main.item_exercise_card_manual_test.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ExampleExerciseCardAdapter(
    private val coroutineScope: CoroutineScope,
    private val exercise: ExampleExerciseToDemonstrateCardsRetesting
) : ListAdapter<ExerciseCard, ExerciseCardViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseCardViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise_card_manual_test, parent, false)
        with(view) {
            layoutParams.apply {
                width = 220.dp
                height = 320.dp
            }
            rememberButton.updateLayoutParams { height = 40.dp }
            rememberButton.textSize = 11f
            notRememberButton.updateLayoutParams { height = 40.dp }
            notRememberButton.textSize = 11f
        }
        return ExerciseCardViewHolder(view, coroutineScope, exercise)
    }

    override fun onBindViewHolder(viewHolder: ExerciseCardViewHolder, position: Int) {
        val exerciseCard: ExerciseCard = getItem(position)
        viewHolder.bind(exerciseCard)
    }

    class ExerciseCardViewHolder(
        itemView: View,
        private val coroutineScope: CoroutineScope,
        private val exercise: ExampleExerciseToDemonstrateCardsRetesting
    ) : RecyclerView.ViewHolder(itemView) {
        private var observing: Job? = null

        fun bind(exerciseCard: ExerciseCard) {
            observing?.cancel()
            observing = coroutineScope.launch {
                with(itemView) {
                    questionTextView.text = exerciseCard.card.question
                    questionScrollView.isVisible = true
                    answerTextView.text = exerciseCard.card.answer
                    exerciseCard.flowOf(ExerciseCard::isAnswerCorrect)
                        .observe(this@launch) { isAnswerCorrect: Boolean? ->
                            answerScrollView.isVisible = isAnswerCorrect != null
                            curtainView.isVisible = isAnswerCorrect == null
                            rememberButton.isSelected = isAnswerCorrect == true
                            notRememberButton.isSelected = isAnswerCorrect == false
                        }
                    rememberButton.setOnClickListener {
                        exercise.setAnswerAsCorrect(exerciseCard)
                    }
                    notRememberButton.setOnClickListener {
                        exercise.setAnswerAsWrong(exerciseCard)
                    }
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ExerciseCard>() {
        override fun areItemsTheSame(oldItem: ExerciseCard, newItem: ExerciseCard): Boolean =
            oldItem.id == newItem.id

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: ExerciseCard, newItem: ExerciseCard): Boolean =
            oldItem == newItem
    }
}