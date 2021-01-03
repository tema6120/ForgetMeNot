package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz

import android.animation.AnimatorInflater
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.Size
import android.util.TypedValue
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.exercise.QuizTestExerciseCard
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.customview.TextViewWithObservableSelection
import com.odnovolov.forgetmenot.presentation.common.dp
import com.odnovolov.forgetmenot.presentation.common.fixTextSelection
import com.odnovolov.forgetmenot.presentation.common.observe
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.AsyncCardFrame
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.CardLabel
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.CardSpaceAllocator
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.ExerciseCardViewHolder
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz.QuizTestExerciseCardEvent.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.quiz.VariantStatus.*
import kotlinx.android.synthetic.main.item_exercise_card_quiz_test.view.*
import kotlinx.android.synthetic.main.popup_card_label_tip.view.*
import kotlinx.coroutines.CoroutineScope

class QuizTestExerciseCardViewHolder(
    private val asyncItemView: AsyncCardFrame,
    private val coroutineScope: CoroutineScope,
    private val controller: BaseController<QuizTestExerciseCardEvent, Nothing>
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

    private val vTextViews by lazy {
        Array(4) {
            TextView(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    setMargins(16.dp, 4.dp, 16.dp, 4.dp)
                    minHeight = 56.dp // if text is smaller than compound drawable
                }
                setPadding(56.dp, 16.dp, 16.dp, 16.dp)
                textSize = 16f
            }
        }
    }

    private val vColumn by lazy {
        LinearLayout(itemView.context).apply {
            layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            orientation = VERTICAL
            setPadding(0, 12.dp, 0, 12.dp)
            vTextViews.forEach(::addView)
        }
    }

    private var cardContent: QuizCardContent? = null
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
            showQuestionButton.setOnClickListener { controller.dispatch(ShowQuestionButtonClicked) }
            questionTextView.textSize = 18f
            questionTextView.observeSelectedText { selection: String ->
                controller.dispatch(QuestionTextSelectionChanged(selection))
            }
            forEachVariantButton { variant: Int ->
                textSize = 16f
                setOnClickListener { controller.dispatch(VariantSelected(variant)) }
                observeSelectedText { selection: String ->
                    controller.dispatch(AnswerTextSelectionChanged(selection))
                }
            }
            val comfortaaFont: Typeface? = ResourcesCompat.getFont(context, R.font.comfortaa)
            cardLabelTextView.setTypeface(comfortaaFont, Typeface.BOLD)
            cardLabelTextView.stateListAnimator =
                AnimatorInflater.loadStateListAnimator(context, R.animator.card_label)
            addScrollListener {
                if (x == 0f) {
                    needToResetRippleOnScrolling = true
                } else {
                    if (needToResetRippleOnScrolling) {
                        needToResetRippleOnScrolling = false
                        showQuestionButton.jumpDrawablesToCurrentState()
                        forEachVariantButton { jumpDrawablesToCurrentState() }
                    }
                }
            }
        }
    }

    private var viewModel: QuizTestExerciseCardViewModel? = null

    override fun bind(exerciseCard: QuizTestExerciseCard) {
        asyncItemView.invokeWhenReady {
            if (viewModel == null) {
                viewModel = QuizTestExerciseCardViewModel(exerciseCard)
                observeViewModel()
            } else {
                questionScrollView.scrollTo(0, 0)
                variantsScrollView.scrollTo(0, 0)
                viewModel!!.setExerciseCard(exerciseCard)
            }
        }
    }

    private fun observeViewModel() {
        with(viewModel!!) {
            with(itemView) {
                cardContent.observe(coroutineScope) { cardContent: QuizCardContent ->
                    this@QuizTestExerciseCardViewHolder.cardContent = cardContent
                }
                isQuestionDisplayed.observe(coroutineScope) { isQuestionDisplayed: Boolean ->
                    showQuestionButton.isVisible = !isQuestionDisplayed
                    questionScrollView.isInvisible = !isQuestionDisplayed
                }
                forEachVariantFrame { variant: Int ->
                    variantStatus(variant).observe(coroutineScope) { variantStatus: VariantStatus ->
                        setVariantBackground(variantFrame = this, variantStatus)
                    }
                }
                forEachVariantButton { variant: Int ->
                    variantStatus(variant).observe(coroutineScope) { variantStatus: VariantStatus ->
                        setVariantIcon(variantButton = this, variantStatus)
                        setVariantTextColor(variantButton = this, variantStatus)
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
        val availableCardHeight = cardSize.height - 1.dp
        val desiredQuestionHeight = measureQuestionHeight(cardContent.question)
        val desiredVariantsHeight = measureVariantsHeight(cardContent.variants)
        CardSpaceAllocator.allocate(
            availableCardHeight,
            itemView.questionFrame,
            desiredQuestionHeight,
            itemView.variantsScrollView,
            desiredVariantsHeight
        )
        itemView.questionTextView.text = cardContent.question
        itemView.questionTextView.fixTextSelection()
        forEachVariantButton { variant: Int ->
            text = cardContent.variants[variant]
        }
    }

    private fun measureQuestionHeight(question: String): Int {
        qTextView.text = question
        qTextView.measure(
            MeasureSpec.makeMeasureSpec(cardSize!!.width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        return qTextView.measuredHeight
    }

    private fun measureVariantsHeight(variants: List<String?>): Int {
        vTextViews.forEachIndexed { index, textView -> textView.text = variants[index] }
        vColumn.measure(
            MeasureSpec.makeMeasureSpec(cardSize!!.width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        return vColumn.measuredHeight
    }

    private inline fun forEachVariantFrame(
        action: View.(variant: Int) -> Unit
    ) {
        with(itemView) {
            variant1Frame.action(0)
            variant2Frame.action(1)
            variant3Frame.action(2)
            variant4Frame.action(3)
        }
    }

    private inline fun forEachVariantButton(
        action: TextViewWithObservableSelection.(variant: Int) -> Unit
    ) {
        with(itemView) {
            variant1Button.action(0)
            variant2Button.action(1)
            variant3Button.action(2)
            variant4Button.action(3)
        }
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

    private fun setVariantTextColor(
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