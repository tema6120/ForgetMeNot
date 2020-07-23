package com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.hideSoftInput
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor.QAEditorEvent.AnswerInputChanged
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor.QAEditorEvent.QuestionInputChanged
import kotlinx.android.synthetic.main.fragment_qa_editor.*
import kotlinx.coroutines.launch

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
            val diScope = CardsEditorDiScope.get()
            val cardId = requireArguments().getLong(ARG_ID)
            controller = diScope.qaEditorController(cardId)
            viewModel = diScope.qaEditorViewModel(cardId)
            observeViewModel()
        }
    }

    private fun setupView() {
        questionEditText.observeText { text: String ->
            controller?.dispatch(QuestionInputChanged(text))
        }
        answerEditText.observeText { text: String ->
            controller?.dispatch(AnswerInputChanged(text))
        }
        reverseCardButton.run {
            setOnClickListener {
                val newAnswer = questionEditText.text
                questionEditText.text = answerEditText.text
                answerEditText.text = newAnswer
            }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        questionPasteButton.run {
            setOnClickListener { questionEditText.paste() }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        answerPasteButton.run {
            setOnClickListener { answerEditText.paste() }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        questionCursorLeftButton.run {
            setOnClickListener { moveQuestionCursorToTheLeft() }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        questionCursorRightButton.run {
            setOnClickListener { moveQuestionCursorToTheRight() }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        answerCursorLeftButton.run {
            setOnClickListener { moveAnswerCursorToTheLeft() }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        answerCursorRightButton.run {
            setOnClickListener { moveAnswerCursorToTheRight() }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        questionClearButton.run {
            setOnClickListener {
                questionEditText.text.clear()
                questionEditText.requestFocus()
            }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        answerClearButton.run {
            setOnClickListener {
                answerEditText.text.clear()
                answerEditText.requestFocus()
            }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
    }

    private fun moveQuestionCursorToTheLeft() {
        when {
            questionEditText.hasSelection() -> questionEditText.moveLeftPinToTheLeft()
            answerEditText.hasSelection() -> answerEditText.moveLeftPinToTheLeft()
            answerEditText.hasFocus() -> answerEditText.moveCursorToTheLeft()
            else -> questionEditText.moveCursorToTheLeft()
        }
    }

    private fun moveQuestionCursorToTheRight() {
        when {
            questionEditText.hasSelection() -> questionEditText.moveLeftPinToTheRight()
            answerEditText.hasSelection() -> answerEditText.moveLeftPinToTheRight()
            answerEditText.hasFocus() -> answerEditText.moveCursorToTheRight()
            else -> questionEditText.moveCursorToTheRight()
        }
    }

    private fun moveAnswerCursorToTheLeft() {
        when {
            questionEditText.hasSelection() -> questionEditText.moveRightPinToTheLeft()
            answerEditText.hasSelection() -> answerEditText.moveRightPinToTheLeft()
            questionEditText.hasFocus() -> questionEditText.moveCursorToTheLeft()
            else -> answerEditText.moveCursorToTheLeft()
        }
    }

    private fun moveAnswerCursorToTheRight() {
        when {
            questionEditText.hasSelection() -> questionEditText.moveRightPinToTheRight()
            answerEditText.hasSelection() -> answerEditText.moveRightPinToTheRight()
            questionEditText.hasFocus() -> questionEditText.moveCursorToTheRight()
            else -> answerEditText.moveCursorToTheRight()
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            questionEditText.setText(question)
            answerEditText.setText(answer)
            isLearned.observe { isLearned: Boolean ->
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

    }

    override fun onPause() {
        super.onPause()
        requireActivity().currentFocus?.hideSoftInput()
    }
}