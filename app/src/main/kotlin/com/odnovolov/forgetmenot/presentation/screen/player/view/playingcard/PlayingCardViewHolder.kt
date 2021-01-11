package com.odnovolov.forgetmenot.presentation.screen.player.view.playingcard

import android.animation.AnimatorInflater
import android.animation.LayoutTransition
import android.graphics.Typeface
import android.util.Size
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.autoplay.PlayingCard
import com.odnovolov.forgetmenot.presentation.common.dp
import com.odnovolov.forgetmenot.presentation.common.fixTextSelection
import com.odnovolov.forgetmenot.presentation.common.observe
import com.odnovolov.forgetmenot.presentation.common.setFont
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.AsyncCardFrame
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.CardSpaceAllocator
import com.odnovolov.forgetmenot.presentation.screen.player.view.playingcard.CardContent.AnsweredCard
import com.odnovolov.forgetmenot.presentation.screen.player.view.playingcard.CardContent.UnansweredCard
import com.odnovolov.forgetmenot.presentation.screen.player.view.playingcard.PlayingCardEvent.*
import kotlinx.android.synthetic.main.item_playing_card.view.*
import kotlinx.android.synthetic.main.popup_card_label_tip.view.*
import kotlinx.coroutines.CoroutineScope

class PlayingCardViewHolder(
    private val asyncItemView: AsyncCardFrame,
    private val coroutineScope: CoroutineScope,
    private val controller: PlayingCardController
) : RecyclerView.ViewHolder(
    asyncItemView
) {
    private val cardLabelTipPopup: PopupWindow by lazy {
        val content = View.inflate(asyncItemView.context, R.layout.popup_card_label_tip, null)
            .apply {
                cardLabelExplanationTextView.setText(R.string.explanation_card_label_learned)
            }
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
            setPadding(16.dp)
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
        with(itemView) {
            cardLinearLayout.layoutTransition.run {
                enableTransitionType(LayoutTransition.CHANGING)
                disableTransitionType(LayoutTransition.APPEARING)
                disableTransitionType(LayoutTransition.DISAPPEARING)
                disableTransitionType(LayoutTransition.CHANGE_APPEARING)
                disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING)
            }
            cardLabelTextView.setOnClickListener {
                showCardLabelTipPopup()
            }
            showQuestionButton.setOnClickListener {
                controller.dispatch(ShowQuestionButtonClicked)
            }
            showAnswerButton.setOnClickListener {
                controller.dispatch(ShowAnswerButtonClicked)
            }
            questionTextView.observeSelectedText { selection: String ->
                controller.dispatch(QuestionTextSelectionChanged(selection))
            }
            questionTextView.textSize = 18f
            answerTextView.observeSelectedText { selection: String ->
                controller.dispatch(AnswerTextSelectionChanged(selection))
            }
            answerTextView.textSize = 16f
            cardLabelTextView.setFont(R.font.comfortaa, Typeface.BOLD)
            cardLabelTextView.stateListAnimator =
                AnimatorInflater.loadStateListAnimator(context, R.animator.card_label)
            asyncItemView.viewTreeObserver.addOnScrollChangedListener {
                if (asyncItemView.x == 0f) {
                    needToResetRippleOnScrolling = true
                } else {
                    if (needToResetRippleOnScrolling) {
                        needToResetRippleOnScrolling = false
                        showQuestionButton.jumpDrawablesToCurrentState()
                        showAnswerButton.jumpDrawablesToCurrentState()
                    }
                }
            }
        }
    }

    private var viewModel: PlayingCardViewModel? = null

    fun bind(playingCard: PlayingCard) {
        asyncItemView.invokeWhenReady {
            if (viewModel == null) {
                viewModel = PlayingCardViewModel(playingCard)
                observeViewModel()
            } else {
                questionScrollView.scrollTo(0, 0)
                answerScrollView.scrollTo(0, 0)
                viewModel!!.setPlayingCard(playingCard)
            }
        }
    }

    private fun observeViewModel() {
        with(viewModel!!) {
            with(itemView) {
                cardContent.observe(coroutineScope) { cardContent: CardContent ->
                    this@PlayingCardViewHolder.cardContent = cardContent
                }
                isQuestionDisplayed.observe(coroutineScope) { isQuestionDisplayed: Boolean ->
                    showQuestionButton.isVisible = !isQuestionDisplayed
                    questionScrollView.isInvisible = !isQuestionDisplayed
                }
                isLearned.observe(coroutineScope) { isLearned: Boolean ->
                    cardLabelTextView.isEnabled = isLearned
                    questionTextView.isActivated = !isLearned
                    answerTextView.isActivated = !isLearned
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
                val desiredAnswerFrameHeight = 48.dp
                CardSpaceAllocator.allocate(
                    availableCardHeight,
                    itemView.questionFrame,
                    desiredQuestionFrameHeight,
                    itemView.answerFrame,
                    desiredAnswerFrameHeight
                )
                itemView.showAnswerButton.isVisible = true
                itemView.answerScrollView.isVisible = false
                itemView.questionTextView.text = cardContent.question
                itemView.questionTextView.fixTextSelection()
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
                itemView.showAnswerButton.isVisible = false
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

    private fun showCardLabelTipPopup() {
        with(cardLabelTipPopup) {
            contentView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            width = contentView.measuredWidth
            height = contentView.measuredHeight
            val xOff: Int = itemView.cardLabelTextView.width / 2 - width / 2
            val yOff: Int = 8.dp
            showAsDropDown(itemView.cardLabelTextView, xOff, yOff)
        }
    }
}