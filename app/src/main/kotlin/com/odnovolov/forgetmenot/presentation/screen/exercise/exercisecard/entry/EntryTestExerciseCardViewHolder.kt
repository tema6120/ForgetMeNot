package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry

import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Paint
import android.view.View
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.domain.interactor.exercise.EntryTestExerciseCard
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.ExerciseCardViewHolder
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry.AnswerStatus.Answered
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry.AnswerStatus.UnansweredWithHint
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry.EntryTestExerciseCardEvent.*
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
            showQuestionButton.setOnClickListener { controller.dispatch(ShowQuestionButtonClicked) }
            questionTextView.observeSelectedText { selection: String ->
                controller.dispatch(QuestionTextSelectionChanged(selection))
            }
            answerEditText.run {
                observeText { text: String -> controller.dispatch(AnswerInputChanged(text)) }
                setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) hideSoftInput() }
            }
            hintTextView.observeSelectedRange { startIndex: Int, endIndex: Int ->
                controller.dispatch(HintSelectionChanged(startIndex, endIndex))
            }
            checkButton.setOnClickListener { controller.dispatch(CheckButtonClicked) }
            wrongAnswerTextView.run {
                observeSelectedText { selection: String ->
                    controller.dispatch(AnswerTextSelectionChanged(selection))
                }
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            correctAnswerTextView.observeSelectedText { selection: String ->
                controller.dispatch(AnswerTextSelectionChanged(selection))
            }
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
                question.observe(coroutineScope) { question: String ->
                    questionTextView.text = question
                    questionTextView.fixTextSelection()
                }
                answerStatus.observe(coroutineScope) { answerStatus: AnswerStatus ->
                    answerInputScrollView.isVisible = answerStatus != Answered
                    hintScrollView.isVisible = answerStatus == UnansweredWithHint
                    hintDivider.isVisible = answerStatus == UnansweredWithHint
                    checkButton.isVisible = answerStatus != Answered
                    answerScrollView.isVisible = answerStatus == Answered
                }
                hint.observe(coroutineScope) { hint: String? ->
                    hintTextView.text = hint
                    hintTextView.fixTextSelection()
                }
                isInputEnabled.observe(coroutineScope, answerEditText::setEnabled)
                wrongAnswer.observe(coroutineScope) { wrongAnswer: String? ->
                    wrongAnswerTextView.isVisible = wrongAnswer != null
                    wrongAnswerTextView.text = wrongAnswer
                }
                correctAnswer.observe(coroutineScope) { correctAnswer: String ->
                    correctAnswerTextView.text = correctAnswer
                    correctAnswerTextView.fixTextSelection()
                }
                isLearned.observe(coroutineScope) { isLearned: Boolean ->
                    val isEnabled = !isLearned
                    showQuestionButton.isEnabled = isEnabled
                    questionTextView.isEnabled = isEnabled
                    wrongAnswerTextView.isEnabled = isEnabled
                    correctAnswerTextView.isEnabled = isEnabled
                }
                answerEditText.setText("")
                questionScrollView.scrollTo(0, 0)
                answerInputScrollView.scrollTo(0, 0)
                hintScrollView.scrollTo(0, 0)
                answerScrollView.scrollTo(0, 0)
            }
        }
    }

    fun onPageSelected() {
        with(itemView.answerEditText) {
            if (isEnabled && resources.configuration.orientation == ORIENTATION_PORTRAIT) {
                showSoftInput()
            }
        }
    }
}