package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.manual

import android.animation.AnimatorInflater
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.exercise.ManualTestExerciseCard
import com.odnovolov.forgetmenot.presentation.common.Stopwatch
import com.odnovolov.forgetmenot.presentation.common.customview.AsyncFrameLayout
import com.odnovolov.forgetmenot.presentation.common.dp
import com.odnovolov.forgetmenot.presentation.common.fixTextSelection
import com.odnovolov.forgetmenot.presentation.common.observe
import com.odnovolov.forgetmenot.presentation.screen.exercise.KnowingWhenPagerStopped
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.CardLabel
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.ExerciseCardViewHolder
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.manual.AnswerStatus.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.manual.ManualTestExerciseCardEvent.*
import kotlinx.android.synthetic.main.item_exercise_card_manual_test.view.*
import kotlinx.coroutines.CoroutineScope

class ManualTestExerciseCardViewHolder(
    private val asyncItemView: AsyncFrameLayout,
    private val coroutineScope: CoroutineScope,
    private val controller: ManualTestExerciseCardController,
    private val knowingWhenPagerStopped: KnowingWhenPagerStopped
) : ExerciseCardViewHolder<ManualTestExerciseCard>(
    asyncItemView
) {
    init {
        asyncItemView.invokeWhenInflated {
            knowingWhenPagerStopped.invokeWhenPagerStopped {
                setupView()
            }
        }
    }

    private fun setupView() {
        with(itemView) {
            showQuestionButton.setOnClickListener {
                controller.dispatch(ShowQuestionButtonClicked)
            }
            questionTextView.observeSelectedText { selection: String ->
                controller.dispatch(QuestionTextSelectionChanged(selection))
            }
            rememberButton.setOnClickListener {
                controller.dispatch(RememberButtonClicked)
            }
            notRememberButton.setOnClickListener {
                controller.dispatch(NotRememberButtonClicked)
            }
            hintTextView.observeSelectedRange { startIndex: Int, endIndex: Int ->
                controller.dispatch(HintSelectionChanged(startIndex, endIndex))
            }
            answerTextView.observeSelectedText { selection: String ->
                controller.dispatch(AnswerTextSelectionChanged(selection))
            }
            val comfortaaFont: Typeface? = ResourcesCompat.getFont(context, R.font.comfortaa)
            rememberButton.setTypeface(comfortaaFont, Typeface.BOLD)
            notRememberButton.setTypeface(comfortaaFont, Typeface.BOLD)
            cardLabelTextView.setTypeface(comfortaaFont, Typeface.BOLD)
            cardLabelTextView.stateListAnimator =
                AnimatorInflater.loadStateListAnimator(context, R.animator.card_label)
        }
    }

    private var viewModel: ManualTestExerciseCardViewModel? = null

    override fun bind(exerciseCard: ManualTestExerciseCard) {
        asyncItemView.invokeWhenInflated {
            if (viewModel == null) {
                knowingWhenPagerStopped.invokeWhenPagerStopped {
                    viewModel = ManualTestExerciseCardViewModel(exerciseCard)
                    observeViewModel()
                }
            } else {
                questionScrollView.scrollTo(0, 0)
                hintScrollView.scrollTo(0, 0)
                answerScrollView.scrollTo(0, 0)
                viewModel!!.setExerciseCard(exerciseCard)
            }
        }
    }

    private fun observeViewModel() {
        with(viewModel!!) {
            with(itemView) {
                isQuestionDisplayed.observe(coroutineScope) { isQuestionDisplayed: Boolean ->
                    showQuestionButton.isVisible = !isQuestionDisplayed
                    questionScrollView.isVisible = isQuestionDisplayed
                }
                question.observe(coroutineScope) { question: String ->
                    questionTextView.text = question
                    questionTextView.fixTextSelection()
                }
                hint.observe(coroutineScope) { hint: String? ->
                    hintTextView.text = hint
                    hintTextView.fixTextSelection()
                }
                answerStatus.observe(coroutineScope) { answerStatus: AnswerStatus ->
                    curtainView.isVisible = answerStatus == Unanswered
                    hintScrollView.isVisible = answerStatus == UnansweredWithHint
                    answerScrollView.isVisible =
                        answerStatus == Correct || answerStatus == Wrong
                    rememberButton.isSelected = answerStatus == Correct
                    notRememberButton.isSelected = answerStatus == Wrong
                    notRememberButton.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        marginStart = when (answerStatus) {
                            Correct, Wrong -> 0.dp
                            else -> 1.dp
                        }
                    }
                    updateShadowColor(answerStatus)
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
                    curtainView.isEnabled = isEnabled
                    rememberButton.isEnabled = isEnabled
                    notRememberButton.isEnabled = isEnabled
                    hintTextView.isEnabled = isEnabled
                    answerTextView.isEnabled = isEnabled
                }
                cardLabel.observe(coroutineScope) { cardLabel: CardLabel? ->
                    when (cardLabel) {
                        CardLabel.Learned -> {
                            cardLabelTextView.setText(R.string.learned)
                            cardLabelTextView.background.setTint(
                                ContextCompat.getColor(context, R.color.card_label_learned)
                            )
                            cardLabelTextView.isEnabled = true
                        }
                        CardLabel.Expired -> {
                            cardLabelTextView.setText(R.string.expired)
                            cardLabelTextView.background.setTint(
                                ContextCompat.getColor(context, R.color.issue)
                            )
                            cardLabelTextView.isEnabled = true
                        }
                        null -> {
                            cardLabelTextView.isEnabled = false
                        }
                    }
                }
            }
        }
    }

    private fun updateShadowColor(answerStatus: AnswerStatus) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.P) return
        with(itemView) {
            updateShadowColor(
                rememberButton,
                answerStatus == Correct,
                R.color.correct_answer_bright
            )
            updateShadowColor(notRememberButton, answerStatus == Wrong, R.color.wrong_answer_bright)
        }
    }

    @SuppressLint("NewApi")
    private fun updateShadowColor(button: View, isSelected: Boolean, selectedColorRes: Int) {
        val shadowColorRes: Int =
            if (isSelected) selectedColorRes
            else R.color.remember_button_not_selected
        val shadowColor: Int = ContextCompat.getColor(button.context, shadowColorRes)
        button.outlineAmbientShadowColor = shadowColor
        button.outlineSpotShadowColor = shadowColor
    }
}