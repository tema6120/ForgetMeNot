package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.exercise.QuizTestExerciseCard
import com.odnovolov.forgetmenot.presentation.common.customview.TextViewWithObservableSelection
import com.odnovolov.forgetmenot.presentation.common.fixTextSelection
import com.odnovolov.forgetmenot.presentation.common.observe
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.AsyncFrameLayout
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.ExerciseCardViewHolder
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz.QuizTestExerciseCardEvent.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz.VariantStatus.*
import kotlinx.android.synthetic.main.item_exercise_card_quiz_test.view.*
import kotlinx.android.synthetic.main.question.view.*
import kotlinx.coroutines.CoroutineScope

class QuizTestExerciseCardViewHolder(
    asyncItemView: AsyncFrameLayout,
    coroutineScope: CoroutineScope,
    private val controller: QuizTestExerciseCardController
) : ExerciseCardViewHolder<QuizTestExerciseCard>(
    asyncItemView,
    coroutineScope
) {
    private val rippleId: Int = getRippleId(itemView.context)

    private fun getRippleId(context: Context): Int {
        val outValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        return outValue.resourceId
    }

    init {
        asyncItemView.invokeWhenInflated {
            showQuestionButton.setOnClickListener { controller.dispatch(ShowQuestionButtonClicked) }
            questionTextView.observeSelectedText { selection: String ->
                controller.dispatch(QuestionTextSelectionChanged(selection))
            }
            forEachVariantButton { variant: Int ->
                setOnClickListener { controller.dispatch(VariantSelected(variant)) }
                observeSelectedText { selection: String ->
                    controller.dispatch(AnswerTextSelectionChanged(selection))
                }
            }
        }
    }

    override fun bind(exerciseCard: QuizTestExerciseCard, coroutineScope: CoroutineScope) {
        val viewModel = QuizTestExerciseCardViewModel(exerciseCard)
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
                forEachVariantButton { variant: Int ->
                    variantText(variant).observe(coroutineScope, ::setText)
                    variantStatus(variant).observe(coroutineScope) { variantStatus: VariantStatus ->
                        setVariantButtonBackground(button = this, variantStatus = variantStatus)
                    }
                }
                isAnswered.observe(coroutineScope) { isAnswered: Boolean ->
                    forEachVariantButton { variant: Int ->
                        if (isAnswered) {
                            setOnClickListener(null)
                            fixTextSelection()
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                // give chance to finish ripple animation
                                postDelayed({ foreground = null }, 600)
                            }
                        } else {
                            setTextIsSelectable(false)
                            setOnClickListener { controller.dispatch(VariantSelected(variant)) }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                foreground = ContextCompat.getDrawable(context, rippleId)
                            }
                        }
                    }
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
                    showQuestionButton.isEnabled = !isLearned
                    questionTextView.isEnabled = !isLearned
                    forEachVariantButton { isEnabled = !isLearned }
                }
                questionScrollView.scrollTo(0, 0)
                variant1ScrollView.scrollTo(0, 0)
                variant2ScrollView.scrollTo(0, 0)
                variant3ScrollView.scrollTo(0, 0)
                variant4ScrollView.scrollTo(0, 0)
            }
        }
    }

    private inline fun View.forEachVariantButton(
        action: TextViewWithObservableSelection.(variant: Int) -> Unit
    ) {
        variant1Button.action(0)
        variant2Button.action(1)
        variant3Button.action(2)
        variant4Button.action(3)
    }

    private fun setVariantButtonBackground(button: View, variantStatus: VariantStatus) {
        with(button) {
            when (variantStatus) {
                Unselected -> {
                    isSelected = false
                }
                Correct -> {
                    isSelected = true
                    background = ContextCompat.getDrawable(
                        context,
                        R.drawable.correct_answer_selector
                    )
                }
                Wrong -> {
                    isSelected = true
                    background = ContextCompat.getDrawable(
                        context,
                        R.drawable.wrong_answer_selector
                    )
                }
            }
        }
    }
}