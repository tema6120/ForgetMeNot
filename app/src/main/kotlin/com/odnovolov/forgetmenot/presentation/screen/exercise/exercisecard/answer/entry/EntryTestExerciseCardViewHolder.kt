package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.entry

import android.graphics.Paint
import android.view.View
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.domain.interactor.exercise.EntryTestExerciseCard
import com.odnovolov.forgetmenot.presentation.common.observe
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.ExerciseCardViewHolder
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.entry.AnswerStatus.Answered
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.entry.AnswerStatus.UnansweredWithHint
import kotlinx.android.synthetic.main.item_exercise_card_entry_test.view.*
import kotlinx.android.synthetic.main.question.view.*
import kotlinx.coroutines.CoroutineScope

class EntryTestExerciseCardViewHolder(
    itemView: View,
    coroutineScope: CoroutineScope,
    controller: EntryTestExerciseCardController
) : ExerciseCardViewHolder<EntryTestExerciseCard>(
    itemView,
    coroutineScope
) {
    init {
        with(itemView) {
            showQuestionButton.setOnClickListener { controller.onShowQuestionButtonClicked() }
            questionTextView.observeSelectedText(controller::onQuestionTextSelectionChanged)
            answerEditText.observeText(controller::onAnswerInputChanged)
            hintTextView.observeSelectedRange(controller::onHintSelectionChanged)
            checkButton.setOnClickListener { controller.onCheckButtonClicked() }
            wrongAnswerTextView.run {
                observeSelectedText(controller::onAnswerTextSelectionChanged)
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            correctAnswerTextView.observeSelectedText(controller::onAnswerTextSelectionChanged)
        }
    }

    override fun bind(exerciseCard: EntryTestExerciseCard, coroutineScope: CoroutineScope) {
        val viewModel = EntryTestExerciseCardViewModel(exerciseCard)
        with(viewModel) {
            with(itemView) {
                isQuestionDisplayed.observe(coroutineScope) { isQuestionDisplayed: Boolean ->
                    showQuestionButton.isVisible = !isQuestionDisplayed
                    questionScrollView.isVisible = isQuestionDisplayed
                }
                question.observe(coroutineScope, questionTextView::setText)
                answerStatus.observe(coroutineScope) { answerStatus: AnswerStatus ->
                    answerInputScrollView.isVisible = answerStatus != Answered
                    hintScrollView.isVisible = answerStatus == UnansweredWithHint
                    hintDivider.isVisible = answerStatus == UnansweredWithHint
                    checkButton.isVisible = answerStatus != Answered
                    answerScrollView.isVisible = answerStatus == Answered
                }
                hint.observe(coroutineScope, hintTextView::setText)
                wrongAnswer.observe(coroutineScope) { wrongAnswer: String? ->
                    wrongAnswerTextView.isVisible = wrongAnswer != null
                    wrongAnswerTextView.text = wrongAnswer
                }
                correctAnswer.observe(coroutineScope, correctAnswerTextView::setText)
                isLearned.observe(coroutineScope) { isLearned: Boolean ->
                    val isEnabled = !isLearned
                    showQuestionButton.isEnabled = isEnabled
                    questionScrollView.isEnabled = isEnabled
                    questionTextView.isEnabled = isEnabled
                    answerInputScrollView.isEnabled = isEnabled
                    hintScrollView.isEnabled = isEnabled
                    answerScrollView.isEnabled = isEnabled
                    wrongAnswerTextView.isEnabled = isEnabled
                    correctAnswerTextView.isEnabled = isEnabled
                }
                answerEditText.setText("")
            }
        }
    }
}