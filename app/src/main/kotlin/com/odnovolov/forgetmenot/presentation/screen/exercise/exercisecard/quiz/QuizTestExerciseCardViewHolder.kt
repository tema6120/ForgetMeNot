package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz

import android.animation.AnimatorInflater
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.view.View.MeasureSpec
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.exercise.QuizTestExerciseCard
import com.odnovolov.forgetmenot.presentation.common.customview.AsyncFrameLayout
import com.odnovolov.forgetmenot.presentation.common.customview.TextViewWithObservableSelection
import com.odnovolov.forgetmenot.presentation.common.dp
import com.odnovolov.forgetmenot.presentation.common.fixTextSelection
import com.odnovolov.forgetmenot.presentation.common.observe
import com.odnovolov.forgetmenot.presentation.screen.exercise.KnowingWhenPagerStopped
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.CardLabel
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.ExerciseCardViewHolder
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz.QuizTestExerciseCardEvent.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz.VariantStatus.*
import kotlinx.android.synthetic.main.item_exercise_card_quiz_test.view.*
import kotlinx.android.synthetic.main.popup_card_label_tip.view.*
import kotlinx.coroutines.CoroutineScope

class QuizTestExerciseCardViewHolder(
    private val asyncItemView: AsyncFrameLayout,
    private val coroutineScope: CoroutineScope,
    private val controller: QuizTestExerciseCardController,
    private val knowingWhenPagerStopped: KnowingWhenPagerStopped
) : ExerciseCardViewHolder<QuizTestExerciseCard>(
    asyncItemView
) {
    private val rippleId: Int = getRippleId(itemView.context)

    private fun getRippleId(context: Context): Int {
        val outValue = TypedValue()
        context.theme.resolveAttribute(
            android.R.attr.selectableItemBackgroundBorderless,
            outValue,
            true
        )
        return outValue.resourceId
    }

    private val cardLabelTipPopup: PopupWindow by lazy {
        val content = View.inflate(asyncItemView.context, R.layout.popup_card_label_tip, null)
        PopupWindow(content).apply {
            setBackgroundDrawable(null)
            isOutsideTouchable = true
            isFocusable = true
            animationStyle = R.style.PopupFromTopAnimation
        }
    }

    init {
        asyncItemView.invokeWhenInflated {
            knowingWhenPagerStopped.invokeWhenPagerStopped {
                setupView()
            }
        }
    }

    private fun setupView() {
        with(itemView) {
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
            val comfortaaFont: Typeface? = ResourcesCompat.getFont(context, R.font.comfortaa)
            cardLabelTextView.setTypeface(comfortaaFont, Typeface.BOLD)
            cardLabelTextView.stateListAnimator =
                AnimatorInflater.loadStateListAnimator(context, R.animator.card_label)
        }
    }

    private var viewModel: QuizTestExerciseCardViewModel? = null

    override fun bind(exerciseCard: QuizTestExerciseCard) {
        asyncItemView.invokeWhenInflated {
            if (viewModel == null) {
                knowingWhenPagerStopped.invokeWhenPagerStopped {
                    viewModel = QuizTestExerciseCardViewModel(exerciseCard)
                    observeViewModel()
                }
            } else {
                questionScrollView.scrollTo(0, 0)
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
                forEachVariantFrame { variant: Int ->
                    variantStatus(variant).observe(coroutineScope) { variantStatus: VariantStatus ->
                        setVariantBackground(variantFrame = this, variantStatus)
                    }
                }
                forEachVariantButton { variant: Int ->
                    variantText(variant).observe(coroutineScope, ::setText)
                    variantStatus(variant).observe(coroutineScope) { variantStatus: VariantStatus ->
                        setVariantIcon(variantButton = this, variantStatus)
                        setVariantText(variantButton = this, variantStatus)
                    }
                }
                isAnswered.observe(coroutineScope) { isAnswered: Boolean ->
                    forEachVariantButton { variant: Int ->
                        if (isAnswered) {
                            setOnClickListener(null)
                            fixTextSelection()
                            // give chance to finish ripple animation
                            postDelayed({ background = null }, 600)
                        } else {
                            setTextIsSelectable(false)
                            setOnClickListener { controller.dispatch(VariantSelected(variant)) }
                            background = ContextCompat.getDrawable(context, rippleId)
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
                    forEachVariantFrame { isEnabled = !isLearned }
                    forEachVariantButton { isEnabled = !isLearned }
                }
                cardLabel.observe(coroutineScope) { cardLabel: CardLabel? ->
                    when (cardLabel) {
                        CardLabel.Learned -> {
                            cardLabelTextView.setText(R.string.learned)
                            cardLabelTextView.background.setTint(
                                ContextCompat.getColor(context, R.color.card_label_learned)
                            )
                            cardLabelTextView.setOnClickListener {
                                showCardLabelTipPopup(cardLabel)
                            }
                            cardLabelTextView.isEnabled = true
                        }
                        CardLabel.Expired -> {
                            cardLabelTextView.setText(R.string.expired)
                            cardLabelTextView.background.setTint(
                                ContextCompat.getColor(context, R.color.issue)
                            )
                            cardLabelTextView.setOnClickListener {
                                showCardLabelTipPopup(cardLabel)
                            }
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

    private inline fun View.forEachVariantFrame(
        action: View.(variant: Int) -> Unit
    ) {
        variant1Frame.action(0)
        variant2Frame.action(1)
        variant3Frame.action(2)
        variant4Frame.action(3)
    }

    private inline fun View.forEachVariantButton(
        action: TextViewWithObservableSelection.(variant: Int) -> Unit
    ) {
        variant1Button.action(0)
        variant2Button.action(1)
        variant3Button.action(2)
        variant4Button.action(3)
    }

    private fun setVariantBackground(variantFrame: View, variantStatus: VariantStatus) {
        val drawableResId: Int = when (variantStatus) {
            WaitingForAnswer -> R.drawable.background_variant_status_wrong_but_not_selected
            Correct -> R.drawable.background_variant_status_correct
            CorrectButNotSelected -> R.drawable.background_variant_status_correct_but_not_selected
            Wrong -> R.drawable.background_variant_status_wrong
            WrongButNotSelected -> R.drawable.background_variant_status_wrong_but_not_selected
        }
        variantFrame.background = ContextCompat.getDrawable(variantFrame.context, drawableResId)
    }

    private fun setVariantIcon(
        variantButton: TextViewWithObservableSelection,
        variantStatus: VariantStatus
    ) {
        val drawableResId: Int = when (variantStatus) {
            Correct -> R.drawable.ic_correct_answer_24
            Wrong -> R.drawable.ic_wrong_answer_24
            else -> R.drawable.ic_radiobutton_unchecked_24
        }
        variantButton.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableResId, 0, 0, 0)
    }

    private fun setVariantText(
        variantButton: TextViewWithObservableSelection,
        variantStatus: VariantStatus
    ) {
        val colorResId: Int = when (variantStatus) {
            Correct -> R.color.text_variant_status_correct
            Wrong -> R.color.text_variant_status_wrong
            else -> R.color.text_secondary_selector
        }
        val colorStateList = ContextCompat.getColorStateList(variantButton.context, colorResId)
        variantButton.setTextColor(colorStateList)
    }

    private fun showCardLabelTipPopup(cardLabel: CardLabel) {
        cardLabelTipPopup.contentView.cardLabelExplanationTextView.setText(
            when (cardLabel) {
                CardLabel.Learned -> R.string.explanation_card_label_learned
                CardLabel.Expired -> R.string.explanation_card_label_expired
            }
        )
        measureCardLabelTipPopup()
        val xOff: Int = itemView.cardLabelTextView.width / 2 - cardLabelTipPopup.width / 2
        val yOff: Int = 8.dp
        cardLabelTipPopup.showAsDropDown(itemView.cardLabelTextView, xOff, yOff)
    }

    private fun measureCardLabelTipPopup() {
        with(cardLabelTipPopup) {
            contentView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            width = contentView.measuredWidth
            height = contentView.measuredHeight
        }
    }
}