package com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor.QAEditorEvent.AnswerInputChanged
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor.QAEditorEvent.QuestionInputChanged
import kotlinx.android.synthetic.main.fragment_qa_editor.*
import kotlinx.android.synthetic.main.item_exercise_card_off_test.view.*
import kotlinx.android.synthetic.main.popup_card_label_tip.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class QAEditorFragment : BaseFragment() {
    companion object {
        const val ARG_ID = "ARG_ID"

        fun create(id: Long) = QAEditorFragment().apply {
            arguments = Bundle(1).apply {
                putLong(ARG_ID, id)
            }
        }
    }

    private var controller: QAEditorController? = null
    private lateinit var viewModel: QAEditorViewModel
    private var resumePauseCoroutineScope: CoroutineScope? = null
    private val cardLabelTipPopup: PopupWindow by lazy {
        val content = View.inflate(context, R.layout.popup_card_label_tip, null)
        PopupWindow(content).apply {
            setBackgroundDrawable(null)
            isOutsideTouchable = true
            isFocusable = true
            animationStyle = R.style.AnimationCardLabel
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_qa_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = CardsEditorDiScope.getAsync() ?: return@launch
            val cardId = requireArguments().getLong(ARG_ID)
            controller = diScope.qaEditorController(cardId)
            viewModel = diScope.qaEditorViewModel(cardId)
            observeViewModel()
        }
    }

    private fun setupView() {
        cardLabelTextView.setOnClickListener {
            showCardLabelTipPopup()
        }
        questionEditText.observeText { text: String ->
            controller?.dispatch(QuestionInputChanged(text))
        }
        answerEditText.observeText { text: String ->
            controller?.dispatch(AnswerInputChanged(text))
        }
        invertCardButton.run {
            setOnClickListener { invertCardWithAnimation() }
            setTooltipTextFromContentDescription()
        }
        questionPasteButton.run {
            setOnClickListener { questionEditText.paste() }
            setTooltipTextFromContentDescription()
        }
        answerPasteButton.run {
            setOnClickListener { answerEditText.paste() }
            setTooltipTextFromContentDescription()
        }
        questionClearButton.run {
            setOnClickListener { questionEditText.text.clear() }
            setTooltipTextFromContentDescription()
        }
        answerClearButton.run {
            setOnClickListener { answerEditText.text.clear() }
            setTooltipTextFromContentDescription()
        }
    }

    private fun showCardLabelTipPopup() {
        with(cardLabelTipPopup) {
            contentView.cardLabelExplanationTextView
                .setText(R.string.explanation_card_label_duplicated)
            contentView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            width = contentView.measuredWidth
            height = contentView.measuredHeight
            val xOff: Int = cardLabelTextView.width / 2 - width / 2
            val yOff: Int = 8.dp
            showAsDropDown(cardLabelTextView, xOff, yOff)
        }
    }

    private fun invertCardWithAnimation() {
        questionEditText.clearFocus()
        answerEditText.clearFocus()
        setViewEnabled(false)
        val secondAnimation = AnimatorSet()
        with(secondAnimation) {
            playTogether(
                ObjectAnimator.ofFloat(cardView, View.ROTATION, 0f),
                ObjectAnimator.ofFloat(questionPasteButton, View.ALPHA, 1f),
                ObjectAnimator.ofFloat(questionClearButton, View.ALPHA, 1f),
                ObjectAnimator.ofFloat(questionEditText, View.ALPHA, 1f),
                ObjectAnimator.ofFloat(answerEditText, View.ALPHA, 1f),
                ObjectAnimator.ofFloat(answerPasteButton, View.ALPHA, 1f),
                ObjectAnimator.ofFloat(answerClearButton, View.ALPHA, 1f)
            )
            duration = 200
            interpolator = LinearInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    setViewEnabled(true)
                }
            })
        }
        AnimatorSet().run {
            playTogether(
                ObjectAnimator.ofFloat(cardView, View.ROTATION, 90f),
                ObjectAnimator.ofFloat(questionPasteButton, View.ALPHA, 0f),
                ObjectAnimator.ofFloat(questionClearButton, View.ALPHA, 0f),
                ObjectAnimator.ofFloat(questionEditText, View.ALPHA, 0f),
                ObjectAnimator.ofFloat(answerEditText, View.ALPHA, 0f),
                ObjectAnimator.ofFloat(answerPasteButton, View.ALPHA, 0f),
                ObjectAnimator.ofFloat(answerClearButton, View.ALPHA, 0f)
            )
            duration = 200
            interpolator = LinearInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    val newAnswer = questionEditText.text
                    questionEditText.text = answerEditText.text
                    answerEditText.text = newAnswer
                    cardView.rotation = -90f
                    secondAnimation.start()
                }
            })
            start()
        }
    }

    private fun setViewEnabled(isEnabled: Boolean) {
        questionPasteButton.isEnabled = isEnabled
        questionClearButton.isEnabled = isEnabled
        questionEditText.isEnabled = isEnabled
        answerEditText.isEnabled = isEnabled
        answerPasteButton.isEnabled = isEnabled
        answerClearButton.isEnabled = isEnabled
        invertCardButton.isEnabled = isEnabled
    }

    private fun observeViewModel() {
        viewCoroutineScope!!.launch {
            val question: String = viewModel.question.first()
            questionEditText.setText(question)
        }
        viewCoroutineScope!!.launch {
            val answer: String = viewModel.answer.first()
            answerEditText.setText(answer)
        }
        viewModel.isLearned.observe { isLearned: Boolean ->
            val color: Int = ContextCompat.getColor(
                requireContext(),
                if (isLearned)
                    R.color.textSecondaryDisabled else
                    R.color.textSecondary
            )
            questionEditText.setTextColor(color)
            answerEditText.setTextColor(color)
        }
    }

    override fun onResume() {
        super.onResume()
        resumePauseCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        resumePauseCoroutineScope!!.launch {
            viewModel.isDuplicated.observe(coroutineScope = this, cardLabelTextView::setEnabled)
        }
    }

    override fun onPause() {
        super.onPause()
        requireActivity().currentFocus?.hideSoftInput()
        resumePauseCoroutineScope!!.cancel()
        resumePauseCoroutineScope = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        controller?.dispose()
    }
}