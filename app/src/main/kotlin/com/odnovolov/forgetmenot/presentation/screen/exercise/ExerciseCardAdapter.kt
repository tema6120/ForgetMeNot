package com.odnovolov.forgetmenot.presentation.screen.exercise

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.exercise.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.AsyncCardFrame
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.ExerciseCardViewHolder
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry.EntryTestExerciseCardEvent
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry.EntryTestExerciseCardViewHolder
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.manual.ManualTestExerciseCardEvent
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.manual.ManualTestExerciseCardViewHolder
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.off.OffTestExerciseCardEvent
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.off.OffTestExerciseCardViewHolder
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz.QuizTestExerciseCardEvent
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz.QuizTestExerciseCardViewHolder
import kotlinx.coroutines.CoroutineScope

class ExerciseCardAdapter(
    private val coroutineScope: CoroutineScope,
    private val offTestExerciseCardController: BaseController<OffTestExerciseCardEvent, Nothing>,
    private val manualTestExerciseCardController: BaseController<ManualTestExerciseCardEvent, Nothing>,
    private val quizTestExerciseCardController: BaseController<QuizTestExerciseCardEvent, Nothing>,
    private val entryTestExerciseCardController: BaseController<EntryTestExerciseCardEvent, Nothing>
) : ListAdapter<ExerciseCard, ExerciseCardViewHolder<ExerciseCard>>(
    DiffCallback()
) {
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is OffTestExerciseCard -> R.layout.item_exercise_card_off_test
            is ManualTestExerciseCard -> R.layout.item_exercise_card_manual_test
            is QuizTestExerciseCard -> R.layout.item_exercise_card_quiz_test
            is EntryTestExerciseCard -> R.layout.item_exercise_card_entry_test
            else -> error("This type of ExerciseCard is not supported")
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ExerciseCardViewHolder<ExerciseCard> {
        val layoutId: Int = viewType
        val itemView = AsyncCardFrame(parent.context)
        itemView.inflateAsync(layoutId)
        return when (layoutId) {
            R.layout.item_exercise_card_off_test -> {
                OffTestExerciseCardViewHolder(
                    itemView,
                    coroutineScope,
                    offTestExerciseCardController
                ) as ExerciseCardViewHolder<ExerciseCard>
            }
            R.layout.item_exercise_card_manual_test -> {
                ManualTestExerciseCardViewHolder(
                    itemView,
                    coroutineScope,
                    manualTestExerciseCardController
                ) as ExerciseCardViewHolder<ExerciseCard>
            }
            R.layout.item_exercise_card_quiz_test -> {
                QuizTestExerciseCardViewHolder(
                    itemView,
                    coroutineScope,
                    quizTestExerciseCardController
                ) as ExerciseCardViewHolder<ExerciseCard>
            }
            R.layout.item_exercise_card_entry_test -> {
                EntryTestExerciseCardViewHolder(
                    itemView,
                    coroutineScope,
                    entryTestExerciseCardController
                ) as ExerciseCardViewHolder<ExerciseCard>
            }
            else -> throw AssertionError()
        }
    }

    override fun onBindViewHolder(viewHolder: ExerciseCardViewHolder<ExerciseCard>, position: Int) {
        val exerciseCard: ExerciseCard = getItem(position)
        viewHolder.bind(exerciseCard)
    }

    private class DiffCallback : DiffUtil.ItemCallback<ExerciseCard>() {
        override fun areItemsTheSame(oldItem: ExerciseCard, newItem: ExerciseCard): Boolean =
            oldItem.base.id == newItem.base.id

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: ExerciseCard, newItem: ExerciseCard): Boolean =
            oldItem == newItem
    }
}