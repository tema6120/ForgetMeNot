package com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.TooltipCompat
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.hideSoftInput
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.showToast
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor.QAEditorEvent.AnswerInputChanged
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor.QAEditorEvent.QuestionInputChanged
import kotlinx.android.synthetic.main.fragment_qa_editor.*

class QAEditorFragment : BaseFragment() {
    companion object {
        const val ARG_ID = "ARG_ID"

        fun create(id: Long) = QAEditorFragment().apply {
            arguments = Bundle(1).apply {
                putLong(ARG_ID, id)
            }
        }
    }

    private var controller: SkeletalQAEditorController? = null
    private var viewModel: QAEditorViewModel? = null
    private var isViewInitialized = false

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
        isViewInitialized = true
        observeViewModel()
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

    private fun EditText.paste() {
        val clipboardText = getClipboardText()
        if (clipboardText == null) {
            showToast(R.string.message_no_paste_data)
            return
        }
        val cursorFinalPosition: Int = selStart + clipboardText.length
        setText(
            StringBuilder(text).run {
                if (hasSelection()) {
                    replace(selStart, selEnd, clipboardText)
                } else {
                    insert(selStart, clipboardText)
                }
            }
        )
        requestFocus()
        setSelection(cursorFinalPosition)
    }

    private fun EditText.getClipboardText(): String? {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (!clipboard.hasPrimaryClip()) return null
        clipboard.primaryClipDescription?.let {
            if (!it.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) return null
        } ?: return null
        return clipboard.primaryClip?.getItemAt(0)?.text.toString()
    }

    private val EditText.selStart get() = minOf(selectionStart, selectionEnd)
    private val EditText.selEnd get() = maxOf(selectionStart, selectionEnd)

    private fun moveQuestionCursorToTheLeft() {
        when {
            questionEditText.hasSelection() -> questionEditText.moveLeftPinToTheLeft()
            answerEditText.hasSelection() -> answerEditText.moveLeftPinToTheLeft()
            answerEditText.hasFocus() -> answerEditText.moveCursorToTheLeft()
            else -> questionEditText.moveCursorToTheLeft()
        }
    }

    private fun EditText.moveLeftPinToTheLeft() {
        if (selStart > 0) {
            setSelection(selStart - 1, selEnd)
        }
    }

    private fun EditText.moveCursorToTheLeft() {
        if (selStart > 0) {
            setSelection(selStart - 1)
        }
        requestFocus()
    }

    private fun moveQuestionCursorToTheRight() {
        when {
            questionEditText.hasSelection() -> questionEditText.moveLeftPinToTheRight()
            answerEditText.hasSelection() -> answerEditText.moveLeftPinToTheRight()
            answerEditText.hasFocus() -> answerEditText.moveCursorToTheRight()
            else -> questionEditText.moveCursorToTheRight()
        }
    }

    private fun EditText.moveLeftPinToTheRight() {
        if (selStart + 1 < selEnd) {
            setSelection(selStart + 1, selEnd)
        }
    }

    private fun EditText.moveCursorToTheRight() {
        if (selStart < text.length) {
            setSelection(selStart + 1)
        }
        requestFocus()
    }

    private fun moveAnswerCursorToTheLeft() {
        when {
            questionEditText.hasSelection() -> questionEditText.moveRightPinToTheLeft()
            answerEditText.hasSelection() -> answerEditText.moveRightPinToTheLeft()
            questionEditText.hasFocus() -> questionEditText.moveCursorToTheLeft()
            else -> answerEditText.moveCursorToTheLeft()
        }
    }

    private fun EditText.moveRightPinToTheLeft() {
        if (selStart < selEnd - 1) {
            setSelection(selStart, selEnd - 1)
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

    private fun EditText.moveRightPinToTheRight() {
        if (selEnd < text.length) {
            setSelection(selStart, selEnd + 1)
        }
    }

    fun inject(controller: SkeletalQAEditorController, viewModel: QAEditorViewModel) {
        this.controller = controller
        this.viewModel = viewModel
        observeViewModel()
    }

    private fun observeViewModel() {
        if (!isViewInitialized || viewModel == null) return
        questionEditText.setText(viewModel!!.question)
        answerEditText.setText(viewModel!!.answer)
    }

    override fun onStop() {
        super.onStop()
        requireActivity().currentFocus?.hideSoftInput()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        isViewInitialized = false
    }
}