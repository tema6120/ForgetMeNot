package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry

import android.animation.AnimatorInflater
import android.animation.LayoutTransition
import android.app.Activity
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.Size
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.doOnNextLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.exercise.EntryTestExerciseCard
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.AsyncCardFrame
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.CardLabel
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.CardSpaceAllocator
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.ExerciseCardViewHolder
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry.CardContent.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry.EntryTestExerciseCardEvent.*
import kotlinx.android.synthetic.main.item_exercise_card_entry_test.view.*
import kotlinx.android.synthetic.main.popup_card_label_tip.view.*
import kotlinx.coroutines.CoroutineScope

class EntryTestExerciseCardViewHolder(
    private val asyncItemView: AsyncCardFrame,
    private val coroutineScope: CoroutineScope,
    private val controller: BaseController<EntryTestExerciseCardEvent, Nothing>
) : ExerciseCardViewHolder<EntryTestExerciseCard>(
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
            setTextSizeFromRes(R.dimen.text_size_question)
        }
    }

    private val hTextView by lazy {
        TextView(itemView.context).apply {
            layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            setPadding(16.dp)
            setTextSizeFromRes(R.dimen.text_size_answer)
        }
    }

    private val wroTextView by lazy {
        TextView(itemView.context).apply {
            layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            setPadding(16.dp)
            setTextSizeFromRes(R.dimen.text_size_answer)
        }
    }

    private val corTextView by lazy {
        TextView(itemView.context).apply {
            layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            setPadding(16.dp)
            setTextSizeFromRes(R.dimen.text_size_answer)
        }
    }

    private val aColumn by lazy {
        LinearLayout(itemView.context).apply {
            layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            orientation = VERTICAL
            addView(wroTextView)
            addView(corTextView)
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
            questionTextView.setTextSizeFromRes(R.dimen.text_size_question)
            hintTextView.observeSelectedRange { startIndex: Int, endIndex: Int ->
                controller.dispatch(HintSelectionChanged(startIndex, endIndex))
            }
            hintTextView.setTextSizeFromRes(R.dimen.text_size_answer)
            answerEditText.run {
                observeText { text: String -> controller.dispatch(AnswerInputChanged(text)) }
                setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) hideKeyboardDelayedIfItIsNotNeeded()
                }
                setTextSizeFromRes(R.dimen.text_size_answer)
            }
            checkButton.setOnClickListener {
                controller.dispatch(CheckButtonClicked)
            }
            wrongAnswerTextView.run {
                observeSelectedText { selection: String ->
                    controller.dispatch(AnswerTextSelectionChanged(selection))
                }
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                setTextSizeFromRes(R.dimen.text_size_answer)
            }
            correctAnswerTextView.observeSelectedText { selection: String ->
                controller.dispatch(AnswerTextSelectionChanged(selection))
            }
            correctAnswerTextView.setTextSizeFromRes(R.dimen.text_size_answer)
            cardLabelTextView.setFont(R.font.comfortaa, Typeface.BOLD)
            checkButton.setFont(R.font.comfortaa, Typeface.BOLD)
            cardLabelTextView.stateListAnimator =
                AnimatorInflater.loadStateListAnimator(context, R.animator.card_label)
            addScrollListener {
                if (x == 0f) {
                    needToResetRippleOnScrolling = true
                } else {
                    if (needToResetRippleOnScrolling) {
                        needToResetRippleOnScrolling = false
                        showQuestionButton.jumpDrawablesToCurrentState()
                        checkButton.jumpDrawablesToCurrentState()
                    }
                }
                checkButton.translationX = x / 3
            }
        }
    }

    private var viewModel: EntryTestExerciseCardViewModel? = null

    override fun bind(exerciseCard: EntryTestExerciseCard) {
        asyncItemView.invokeWhenReady {
            if (viewModel == null) {
                viewModel = EntryTestExerciseCardViewModel(exerciseCard)
                observeViewModel()
            } else {
                questionScrollView.scrollTo(0, 0)
                answerInputScrollView.scrollTo(0, 0)
                hintScrollView.scrollTo(0, 0)
                answerScrollView.scrollTo(0, 0)
                answerEditText.setText(exerciseCard.userInput)
                answerEditText.setSelection(exerciseCard.userInput?.length ?: 0)
                asyncItemView.doOnNextLayout {
                    checkButton.translationX = asyncItemView.x / 3
                }
                viewModel!!.setExerciseCard(exerciseCard)
            }
        }
    }

    private fun observeViewModel() {
        with(viewModel!!) {
            with(itemView) {
                cardContent.observe(coroutineScope) { cardContent: CardContent ->
                    this@EntryTestExerciseCardViewHolder.cardContent = cardContent
                }
                isQuestionDisplayed.observe(coroutineScope) { isQuestionDisplayed: Boolean ->
                    showQuestionButton.isVisible = !isQuestionDisplayed
                    questionScrollView.isInvisible = !isQuestionDisplayed
                }
                isInputEnabled.observe(coroutineScope) { isEnabled: Boolean ->
                    answerEditText.isEnabled = isEnabled
                    if (isEnabled) {
                        answerEditText.post {
                            if (answerEditText.isVisibleOnScreen()) {
                                answerEditText.showSoftInput()
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
                isLearned.observe(coroutineScope) { isLearned: Boolean ->
                    val isEnabled = !isLearned
                    showQuestionButton.isEnabled = isEnabled
                    questionTextView.isEnabled = isEnabled
                    hintTextView.isEnabled = isEnabled
                    checkButton.isEnabled = isEnabled
                    wrongAnswerTextView.isEnabled = isEnabled
                    correctAnswerTextView.isEnabled = isEnabled
                }
                cardLabel.observe(coroutineScope) { cardLabel: CardLabel? ->
                    when (cardLabel) {
                        CardLabel.Learned -> {
                            cardLabelTextView.setText(R.string.card_label_learned)
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
                            cardLabelTextView.setText(R.string.card_label_expired)
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
                val desiredInputFrameHeight = availableCardHeight
                CardSpaceAllocator.allocate(
                    availableCardHeight,
                    itemView.questionFrame,
                    desiredQuestionFrameHeight,
                    itemView.answerInputScrollView,
                    desiredInputFrameHeight
                )
                itemView.hintScrollView.isVisible = false
                itemView.hintDivider.isVisible = false
                itemView.answerInputScrollView.isVisible = true
                itemView.answerScrollView.isVisible = false
                itemView.checkButton.isVisible = true
                itemView.questionTextView.text = cardContent.question
                itemView.questionTextView.fixTextSelection()
            }
            is UnansweredCardWithHint -> {
                val availableCardHeight = cardSize.height - 2.dp
                val desiredQuestionHeight = measureHeight(qTextView, cardContent.question)
                val desiredHintHeight = measureHeight(hTextView, cardContent.hint)
                CardSpaceAllocatorForUnansweredSpellingCardWithHint.allocate(
                    availableCardHeight,
                    itemView.questionFrame,
                    desiredQuestionHeight,
                    itemView.hintScrollView,
                    desiredHintHeight,
                    itemView.answerInputScrollView
                )
                itemView.hintScrollView.isVisible = true
                itemView.hintDivider.isVisible = true
                itemView.answerInputScrollView.isVisible = true
                itemView.answerScrollView.isVisible = false
                itemView.checkButton.isVisible = true
                itemView.questionTextView.text = cardContent.question
                itemView.questionTextView.fixTextSelection()
                itemView.hintTextView.text = cardContent.hint
                itemView.hintTextView.fixTextSelection()
            }
            is AnsweredCard -> {
                val availableCardHeight = cardSize.height - 1.dp
                val desiredQuestionFrameHeight = measureHeight(qTextView, cardContent.question)
                val desiredAnswerFrameHeight = measureAnswerFrame(cardContent)
                CardSpaceAllocator.allocate(
                    availableCardHeight,
                    itemView.questionFrame,
                    desiredQuestionFrameHeight,
                    itemView.answerScrollView,
                    desiredAnswerFrameHeight
                )
                itemView.hintScrollView.isVisible = false
                itemView.hintDivider.isVisible = false
                itemView.answerInputScrollView.isVisible = false
                itemView.answerScrollView.isVisible = true
                itemView.checkButton.isVisible = false
                itemView.questionTextView.text = cardContent.question
                itemView.questionTextView.fixTextSelection()
                itemView.wrongAnswerTextView.isVisible = cardContent.wrongAnswer != null
                if (cardContent.wrongAnswer != null) {
                    itemView.wrongAnswerTextView.text = cardContent.wrongAnswer
                    itemView.wrongAnswerTextView.fixTextSelection()
                }
                itemView.correctAnswerTextView.text = cardContent.correctAnswer
                itemView.correctAnswerTextView.fixTextSelection()
            }
        }
        itemView.cardLinearLayout.requestLayout()
    }

    private fun measureHeight(textView: TextView, question: String): Int {
        textView.text = question
        textView.measure(
            MeasureSpec.makeMeasureSpec(cardSize!!.width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        return textView.measuredHeight
    }

    private fun measureAnswerFrame(cardContent: AnsweredCard): Int {
        wroTextView.isVisible = cardContent.wrongAnswer != null
        if (cardContent.wrongAnswer != null) {
            wroTextView.text = cardContent.wrongAnswer
        }
        corTextView.text = cardContent.correctAnswer
        aColumn.measure(
            MeasureSpec.makeMeasureSpec(cardSize!!.width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        return aColumn.measuredHeight
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

    fun onPageSelected() {
        asyncItemView.invokeWhenReady {
            if (answerEditText.isEnabled
                && resources.configuration.orientation == ORIENTATION_PORTRAIT
            ) {
                answerEditText.showSoftInput()
            }
        }
    }

    private fun hideKeyboardDelayedIfItIsNotNeeded() {
        val activity = itemView.getActivity() ?: return
        itemView.postDelayed({
            val focusedView = activity.currentFocus ?: return@postDelayed
            if (isKeyboardVisible(focusedView) != true) return@postDelayed
            if (focusedView !is AppCompatEditText || !focusedView.isVisibleOnScreen()) {
                val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE)
                        as InputMethodManager
                imm.toggleSoftInput(0, 0)
            }
        }, 50)
    }
}