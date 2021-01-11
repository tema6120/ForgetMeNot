package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.manual

import android.animation.AnimatorInflater
import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.util.Size
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.PopupWindow
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.*
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.exercise.ManualTestExerciseCard
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.dp
import com.odnovolov.forgetmenot.presentation.common.fixTextSelection
import com.odnovolov.forgetmenot.presentation.common.observe
import com.odnovolov.forgetmenot.presentation.common.setFont
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.AsyncCardFrame
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.CardLabel
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.CardSpaceAllocator
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.ExerciseCardViewHolder
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.manual.CardContent.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.manual.ManualTestExerciseCardEvent.*
import kotlinx.android.synthetic.main.item_exercise_card_manual_test.view.*
import kotlinx.android.synthetic.main.popup_card_label_tip.view.*
import kotlinx.coroutines.CoroutineScope

class ManualTestExerciseCardViewHolder(
    private val asyncItemView: AsyncCardFrame,
    private val coroutineScope: CoroutineScope,
    private val controller: BaseController<ManualTestExerciseCardEvent, Nothing>
) : ExerciseCardViewHolder<ManualTestExerciseCard>(
    asyncItemView
) {
    private val cardLabelTipPopup: PopupWindow by lazy {
        val content = View.inflate(asyncItemView.context, R.layout.popup_card_label_tip, null)
        PopupWindow(content).apply {
            setBackgroundDrawable(null)
            isOutsideTouchable = true
            isFocusable = true
            animationStyle = R.style.AnimationCardLabel
        }
    }

    private val qTextView by lazy {
        TextView(itemView.context).apply {
            layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            setPadding(16.dp)
            textSize = 18f
        }
    }

    private val aTextView by lazy {
        TextView(itemView.context).apply {
            layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            setPadding(16.dp, 16.dp, 16.dp, 80.dp)
            textSize = 16f
        }
    }

    private var cardContent: CardContent? = null
        set(value) {
            field = value
            updateCardContent()
        }

    private var cardSize: Size? = null
        set(value) {
            itemView.post {
                if (field != value) {
                    field = value
                    updateCardContent()
                }
            }
        }

    private var needToResetRippleOnScrolling = true

    init {
        asyncItemView.invokeWhenReady {
            cardView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                cardSize = Size(cardView.width, cardView.height)
            }
            setupView()
        }
    }

    private fun setupView() {
        with(asyncItemView) {
            cardLinearLayout.layoutTransition.run {
                enableTransitionType(LayoutTransition.CHANGING)
                disableTransitionType(LayoutTransition.APPEARING)
                disableTransitionType(LayoutTransition.DISAPPEARING)
                disableTransitionType(LayoutTransition.CHANGE_APPEARING)
                disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING)
            }
            showQuestionButton.setOnClickListener {
                controller.dispatch(ShowQuestionButtonClicked)
            }
            questionTextView.observeSelectedText { selection: String ->
                controller.dispatch(QuestionTextSelectionChanged(selection))
            }
            questionTextView.textSize = 18f
            rememberButton.setOnClickListener {
                controller.dispatch(RememberButtonClicked)
            }
            notRememberButton.setOnClickListener {
                controller.dispatch(NotRememberButtonClicked)
            }
            hintTextView.observeSelectedRange { startIndex: Int, endIndex: Int ->
                controller.dispatch(HintSelectionChanged(startIndex, endIndex))
            }
            hintTextView.textSize = 16f
            answerTextView.observeSelectedText { selection: String ->
                controller.dispatch(AnswerTextSelectionChanged(selection))
            }
            answerTextView.textSize = 16f
            rememberButton.setFont(R.font.comfortaa, Typeface.BOLD)
            notRememberButton.setFont(R.font.comfortaa, Typeface.BOLD)
            cardLabelTextView.setFont(R.font.comfortaa, Typeface.BOLD)
            cardLabelTextView.stateListAnimator =
                AnimatorInflater.loadStateListAnimator(context, R.animator.card_label)
            addScrollListener {
                if (x == 0f) {
                    needToResetRippleOnScrolling = true
                } else {
                    if (needToResetRippleOnScrolling) {
                        needToResetRippleOnScrolling = false
                        showQuestionButton.jumpDrawablesToCurrentState()
                        rememberButton.jumpDrawablesToCurrentState()
                        notRememberButton.jumpDrawablesToCurrentState()
                    }
                }
                bottomButtonsLayout.translationX = x / 3
            }
        }
    }

    private var viewModel: ManualTestExerciseCardViewModel? = null

    override fun bind(exerciseCard: ManualTestExerciseCard) {
        asyncItemView.invokeWhenReady {
            if (viewModel == null) {
                viewModel = ManualTestExerciseCardViewModel(exerciseCard)
                observeViewModel()
            } else {
                questionScrollView.scrollTo(0, 0)
                hintScrollView.scrollTo(0, 0)
                answerScrollView.scrollTo(0, 0)
                asyncItemView.doOnNextLayout {
                    bottomButtonsLayout.translationX = asyncItemView.x / 3
                }
                viewModel!!.setExerciseCard(exerciseCard)
            }
        }
    }

    private fun observeViewModel() {
        with(viewModel!!) {
            with(itemView) {
                cardContent.observe(coroutineScope) { cardContent: CardContent ->
                    this@ManualTestExerciseCardViewHolder.cardContent = cardContent
                }
                isQuestionDisplayed.observe(coroutineScope) { isQuestionDisplayed: Boolean ->
                    showQuestionButton.isVisible = !isQuestionDisplayed
                    questionScrollView.isInvisible = !isQuestionDisplayed
                }
                isAnswerCorrect.observe(coroutineScope) { isAnswerCorrect: Boolean? ->
                    rememberButton.isSelected = isAnswerCorrect == true
                    notRememberButton.isSelected = isAnswerCorrect == false
                    notRememberButton.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        marginStart = if (isAnswerCorrect == null) 1.dp else 0.dp
                    }
                    updateBottomButtonsShadowColor()
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
                            cardLabelTextView.background.colorFilter =
                                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                    ContextCompat.getColor(context, R.color.card_label_learned),
                                    BlendModeCompat.SRC_ATOP
                                )
                            cardLabelTextView.setOnClickListener {
                                showCardLabelTipPopup(cardLabel)
                            }
                            cardLabelTextView.isEnabled = true
                        }
                        CardLabel.Expired -> {
                            cardLabelTextView.setText(R.string.expired)
                            cardLabelTextView.background.colorFilter =
                                BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                                    ContextCompat.getColor(context, R.color.issue),
                                    BlendModeCompat.SRC_ATOP
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

    private fun updateCardContent() {
        val cardContent = cardContent ?: return
        val cardSize = cardSize ?: return
        when (cardContent) {
            is UnansweredCard -> {
                val availableCardHeight = cardSize.height - 1.dp
                val desiredQuestionFrameHeight = measureHeight(qTextView, cardContent.question)
                val desiredAnswerFrameHeight = 80.dp
                CardSpaceAllocator.allocate(
                    availableCardHeight,
                    itemView.questionFrame,
                    desiredQuestionFrameHeight,
                    itemView.answerFrame,
                    desiredAnswerFrameHeight
                )
                itemView.hintScrollView.isVisible = false
                itemView.answerFrame.isVisible = true
                itemView.curtainView.isVisible = true
                itemView.answerScrollView.isVisible = false
                itemView.questionTextView.text = cardContent.question
                itemView.questionTextView.fixTextSelection()
            }
            is UnansweredCardWithHint -> {
                val availableCardHeight = cardSize.height - 1.dp
                val desiredQuestionFrameHeight = measureHeight(qTextView, cardContent.question)
                val desiredHintFrameHeight = measureHeight(aTextView, cardContent.hint)
                CardSpaceAllocator.allocate(
                    availableCardHeight,
                    itemView.questionFrame,
                    desiredQuestionFrameHeight,
                    itemView.hintScrollView,
                    desiredHintFrameHeight
                )
                itemView.hintScrollView.isVisible = true
                itemView.answerFrame.isVisible = false
                itemView.curtainView.isVisible = false
                itemView.answerScrollView.isVisible = false
                itemView.questionTextView.text = cardContent.question
                itemView.questionTextView.fixTextSelection()
                itemView.hintTextView.text = cardContent.hint
                itemView.hintTextView.fixTextSelection()
            }
            is AnsweredCard -> {
                val availableCardHeight = cardSize.height - 1.dp
                val desiredQuestionFrameHeight = measureHeight(qTextView, cardContent.question)
                val desiredAnswerFrameHeight = measureHeight(aTextView, cardContent.answer)
                CardSpaceAllocator.allocate(
                    availableCardHeight,
                    itemView.questionFrame,
                    desiredQuestionFrameHeight,
                    itemView.answerFrame,
                    desiredAnswerFrameHeight
                )
                itemView.hintScrollView.isVisible = false
                itemView.answerFrame.isVisible = true
                itemView.curtainView.isVisible = false
                itemView.answerScrollView.isVisible = true
                itemView.questionTextView.text = cardContent.question
                itemView.questionTextView.fixTextSelection()
                itemView.answerTextView.text = cardContent.answer
                itemView.answerTextView.fixTextSelection()
            }
        }
    }

    private fun measureHeight(textView: TextView, question: String): Int {
        textView.text = question
        textView.measure(
            MeasureSpec.makeMeasureSpec(cardSize!!.width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        return textView.measuredHeight
    }

    private fun updateBottomButtonsShadowColor() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.P) return
        updateBottomButtonsShadowColor(itemView.rememberButton, R.color.correct_answer_bright)
        updateBottomButtonsShadowColor(itemView.notRememberButton, R.color.wrong_answer_bright)
    }

    @SuppressLint("NewApi")
    private fun updateBottomButtonsShadowColor(button: View, selectedColorRes: Int) {
        val shadowColorRes: Int =
            if (button.isSelected) selectedColorRes
            else R.color.floating_button_in_exercise
        val shadowColor: Int = ContextCompat.getColor(button.context, shadowColorRes)
        button.outlineAmbientShadowColor = shadowColor
        button.outlineSpotShadowColor = shadowColor
    }

    private fun showCardLabelTipPopup(cardLabel: CardLabel) {
        with(cardLabelTipPopup) {
            contentView.cardLabelExplanationTextView.setText(
                when (cardLabel) {
                    CardLabel.Learned -> R.string.explanation_card_label_learned
                    CardLabel.Expired -> R.string.explanation_card_label_expired
                }
            )
            contentView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            width = contentView.measuredWidth
            height = contentView.measuredHeight
            val xOff: Int = itemView.cardLabelTextView.width / 2 - width / 2
            val yOff: Int = 8.dp
            showAsDropDown(itemView.cardLabelTextView, xOff, yOff)
        }
    }
}