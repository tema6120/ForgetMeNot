package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.exercise.QuizTestExerciseCard
import com.odnovolov.forgetmenot.presentation.common.customview.TextViewWithObservableSelection
import com.odnovolov.forgetmenot.presentation.common.fixTextSelection
import com.odnovolov.forgetmenot.presentation.common.observe
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.ExerciseCardViewHolder
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz.VariantStatus.*
import kotlinx.android.synthetic.main.item_exercise_card_quiz_test.view.*
import kotlinx.android.synthetic.main.question.view.*
import kotlinx.coroutines.CoroutineScope

class QuizTestExerciseCardViewHolder(
    itemView: View,
    coroutineScope: CoroutineScope,
    private val controller: QuizTestExerciseCardController
) : ExerciseCardViewHolder<QuizTestExerciseCard>(
    itemView,
    coroutineScope
) {
    private val rippleId: Int = getRippleId(itemView.context)
    private val vibrator: Vibrator? =
        ContextCompat.getSystemService(itemView.context, Vibrator::class.java)

    private fun getRippleId(context: Context): Int {
        val outValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        return outValue.resourceId
    }

    init {
        with(itemView) {
            showQuestionButton.setOnClickListener { controller.onShowQuestionButtonClicked() }
            questionTextView.observeSelectedText(controller::onQuestionTextSelectionChanged)
            forEachVariantButton { variant: Int ->
                setOnClickListener { controller.onVariantSelected(variant) }
                observeSelectedText(controller::onAnswerTextSelectionChanged)
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
                        } else {
                            setTextIsSelectable(false)
                            setOnClickListener { controller.onVariantSelected(variant) }
                        }
                    }
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
                    setBackgroundResource(rippleId)
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
                Unaffected -> {
                    isSelected = false
                    background = null
                }
            }
        }
    }

    private fun vibrate() {
        vibrator?.let { vibrator: Vibrator ->
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        VIBRATION_DURATION,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(VIBRATION_DURATION)
            }
        }
    }

    companion object {
        private const val VIBRATION_DURATION = 50L
    }
}