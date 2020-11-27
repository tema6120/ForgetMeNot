package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.off

import android.graphics.Color
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.exercise.OffTestExerciseCard
import com.odnovolov.forgetmenot.presentation.common.dp
import com.odnovolov.forgetmenot.presentation.common.fixTextSelection
import com.odnovolov.forgetmenot.presentation.common.observe
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.AsyncFrameLayout
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.ExerciseCardViewHolder
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.off.AnswerStatus.Answered
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.off.AnswerStatus.UnansweredWithHint
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.off.OffTestExerciseCardEvent.*
import kotlinx.android.synthetic.main.item_exercise_card_off_test.view.*
import kotlinx.coroutines.CoroutineScope

class OffTestExerciseCardViewHolder(
    asyncItemView: AsyncFrameLayout,
    coroutineScope: CoroutineScope,
    controller: OffTestExerciseCardController
) : ExerciseCardViewHolder<OffTestExerciseCard>(
    asyncItemView,
    coroutineScope
) {
    init {
        asyncItemView.invokeWhenInflated {
            showQuestionButton.setOnClickListener {
                controller.dispatch(ShowQuestionButtonClicked)
            }
            questionTextView.observeSelectedText { selection: String ->
                controller.dispatch(QuestionTextSelectionChanged(selection))
            }
            showAnswerButton.setOnClickListener {
                controller.dispatch(ShowAnswerButtonClicked)
            }
            hintTextView.observeSelectedRange { startIndex: Int, endIndex: Int ->
                controller.dispatch(HintSelectionChanged(startIndex, endIndex))
            }
            answerTextView.observeSelectedText { selection: String ->
                controller.dispatch(AnswerTextSelectionChanged(selection))
            }
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
                question.observe(coroutineScope) { question: String ->
                    questionTextView.text = question
                    questionTextView.fixTextSelection()
                }
                answerStatus.observe(coroutineScope) { answerStatus: AnswerStatus ->
                    showAnswerFrame.isVisible = answerStatus != Answered
                    showAnswerFrame.updateLayoutParams {
                        height = if (answerStatus == UnansweredWithHint) 48.dp else MATCH_PARENT
                    }
                    hintScrollView.isVisible = answerStatus == UnansweredWithHint
                    hintDivider.isVisible = answerStatus == UnansweredWithHint
                    answerScrollView.isVisible = answerStatus == Answered
                }
                hint.observe(coroutineScope) { hint: String? ->
                    hintTextView.text = hint
                    hintTextView.fixTextSelection()
                }
                answer.observe(coroutineScope) { answer: String ->
                    answerTextView.text = answer
                    answerTextView.fixTextSelection()
                }
                isExpired.observe(coroutineScope) { isExpired: Boolean ->
                    val cardBackgroundColor: Int =
                        if (isExpired) {
                            ContextCompat.getColor(context, R.color.background_expired_card)
                        } else {
                            Color.WHITE
                        }
                    cardView.setCardBackgroundColor(cardBackgroundColor)
                }
                vibrateCommand.observe(coroutineScope) { vibrate() }
                isLearned.observe(coroutineScope) { isLearned: Boolean ->
                    val isEnabled = !isLearned
                    showQuestionButton.isEnabled = isEnabled
                    questionTextView.isEnabled = isEnabled
                    showAnswerButton.isEnabled = isEnabled
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