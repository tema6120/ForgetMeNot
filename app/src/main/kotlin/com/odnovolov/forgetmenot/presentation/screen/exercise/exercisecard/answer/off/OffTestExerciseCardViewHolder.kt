package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.off

import android.view.View
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.domain.interactor.exercise.OffTestExerciseCard
import com.odnovolov.forgetmenot.presentation.common.observe
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.ExerciseCardViewHolder
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.off.AnswerStatus.Answered
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.off.AnswerStatus.UnansweredWithHint
import kotlinx.android.synthetic.main.item_exercise_card_off_test.view.*
import kotlinx.android.synthetic.main.question.view.*
import kotlinx.coroutines.CoroutineScope

class OffTestExerciseCardViewHolder(
    itemView: View,
    coroutineScope: CoroutineScope,
    controller: OffTestExerciseCardController
) : ExerciseCardViewHolder<OffTestExerciseCard>(
    itemView,
    coroutineScope
) {
    init {
        with(itemView) {
            showQuestionButton.setOnClickListener { controller.onShowQuestionButtonClicked() }
            questionTextView.observeSelectedText(controller::onQuestionTextSelectionChanged)
            showAnswerButton.setOnClickListener { controller.onShowAnswerButtonClicked() }
            hintTextView.observeSelectedRange(controller::onHintSelectionChanged)
            answerTextView.observeSelectedText(controller::onAnswerTextSelectionChanged)
        }
    }

    override fun bind(exerciseCard: OffTestExerciseCard, coroutineScope: CoroutineScope) {
        val viewModel = OffTestExerciseCardViewModel(exerciseCard)
        with(viewModel) {
            with(itemView) {
                isQuestionDisplayed.observe(coroutineScope) { isQuestionDisplayed: Boolean ->
                    showQuestionButton.isVisible = !isQuestionDisplayed
                    questionScrollView.isVisible = isQuestionDisplayed
                }
                question.observe(coroutineScope) {
                    questionTextView.setText(it)
                }
                answerStatus.observe(coroutineScope) { answerStatus: AnswerStatus ->
                    showAnswerButton.isVisible = answerStatus != Answered
                    hintScrollView.isVisible = answerStatus == UnansweredWithHint
                    answerScrollView.isVisible = answerStatus == Answered
                }
                hint.observe(coroutineScope) {
                    hintTextView.setText(it)
                }
                answer.observe(coroutineScope) {
                    answerTextView.setText(it)
                }
                isLearned.observe(coroutineScope) { isLearned: Boolean ->
                    val isEnabled = !isLearned
                    showQuestionButton.isEnabled = isEnabled
                    questionScrollView.isEnabled = isEnabled
                    questionTextView.isEnabled = isEnabled
                    showAnswerButton.isEnabled = isEnabled
                    hintScrollView.isEnabled = isEnabled
                    answerScrollView.isEnabled = isEnabled
                    hintTextView.isEnabled = isEnabled
                    answerTextView.isEnabled = isEnabled
                }
            }
        }
    }
}