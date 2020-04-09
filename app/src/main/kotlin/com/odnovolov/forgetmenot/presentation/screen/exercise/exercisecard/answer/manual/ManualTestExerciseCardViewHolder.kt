package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.manual

import android.view.View
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.domain.interactor.exercise.ManualTestExerciseCard
import com.odnovolov.forgetmenot.presentation.common.fixTextSelection
import com.odnovolov.forgetmenot.presentation.common.observe
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.ExerciseCardViewHolder
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.manual.AnswerStatus.*
import kotlinx.android.synthetic.main.item_exercise_card_manual_test.view.*
import kotlinx.android.synthetic.main.question.view.*
import kotlinx.coroutines.CoroutineScope

class ManualTestExerciseCardViewHolder(
    itemView: View,
    coroutineScope: CoroutineScope,
    controller: ManualTestExerciseCardController
) : ExerciseCardViewHolder<ManualTestExerciseCard>(
    itemView,
    coroutineScope
) {
    init {
        with(itemView) {
            showQuestionButton.setOnClickListener { controller.onShowQuestionButtonClicked() }
            questionTextView.observeSelectedText(controller::onQuestionTextSelectionChanged)
            rememberButton.setOnClickListener { controller.onRememberButtonClicked() }
            notRememberButton.setOnClickListener { controller.onNotRememberButtonClicked() }
            hintTextView.observeSelectedRange(controller::onHintSelectionChanged)
            answerTextView.observeSelectedText(controller::onAnswerTextSelectionChanged)
        }
    }

    override fun bind(exerciseCard: ManualTestExerciseCard, coroutineScope: CoroutineScope) {
        val viewModel = ManualTestExerciseCardViewModel(exerciseCard)
        with(viewModel) {
            with(itemView) {
                isQuestionDisplayed.observe(coroutineScope) { isQuestionDisplayed: Boolean ->
                    showQuestionButton.isVisible = !isQuestionDisplayed
                    questionScrollView.isVisible = isQuestionDisplayed
                }
                question.observe(coroutineScope) { question: String ->
                    questionTextView.text = question
                    questionTextView.fixTextSelection()
                }
                answerStatus.observe(coroutineScope) { answerStatus: AnswerStatus ->
                    curtainView.isVisible = answerStatus == Unanswered
                    hintScrollView.isVisible = answerStatus == UnansweredWithHint
                    answerScrollView.isVisible = answerStatus == Correct || answerStatus == Wrong
                    rememberButton.isSelected = answerStatus == Correct
                    notRememberButton.isSelected = answerStatus == Wrong
                }
                hint.observe(coroutineScope) { hint: String? ->
                    hintTextView.text = hint
                    hintTextView.fixTextSelection()
                }
                answer.observe(coroutineScope) { answer: String ->
                    answerTextView.text = answer
                    answerTextView.fixTextSelection()
                }
                isLearned.observe(coroutineScope) { isLearned: Boolean ->
                    val isEnabled = !isLearned
                    showQuestionButton.isEnabled = isEnabled
                    questionTextView.isEnabled = isEnabled
                    curtainView.isEnabled = isEnabled
                    rememberButton.isEnabled = isEnabled
                    notRememberButton.isEnabled = isEnabled
                    hintTextView.isEnabled = isEnabled
                    answerTextView.isEnabled = isEnabled
                }
                questionScrollView.scrollTo(0, 0)
                hintScrollView.scrollTo(0, 0)
                answerScrollView.scrollTo(0, 0)
            }
        }
    }
}