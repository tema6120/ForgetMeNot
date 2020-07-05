package com.odnovolov.forgetmenot.presentation.screen.editcard

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
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.showToast
import com.odnovolov.forgetmenot.presentation.screen.editcard.EditCardController.Command.UpdateQuestionAndAnswer
import com.odnovolov.forgetmenot.presentation.screen.editcard.EditCardEvent.*
import kotlinx.android.synthetic.main.fragment_edit_card.*
import kotlinx.android.synthetic.main.item_card_editor.*
import kotlinx.coroutines.launch

class EditCardFragment : BaseFragment() {
    init {
        EditCardDiScope.reopenIfClosed()
    }

    private lateinit var viewModel: EditCardViewModel
    private var controller: EditCardController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_card, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = EditCardDiScope.get()
            controller = diScope.controller
            viewModel = diScope.viewModel
            if (savedInstanceState == null) {
                updateText()
            }
            viewModel.isAcceptButtonEnabled.observe(acceptButton::setEnabled)
            controller!!.commands.observe(::executeCommand)
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
            setOnClickListener { controller?.dispatch(ReverseCardButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        cancelButton.run {
            setOnClickListener { controller?.dispatch(CancelButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        acceptButton.run {
            setOnClickListener { controller?.dispatch(AcceptButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
            isEnabled = false
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
        when {
            clipboardText == null -> {
                showToast(R.string.message_no_paste_data)
            }
            hasSelection() -> {
                val cursorFinalPosition = selStart + clipboardText.length
                text = text.replace(selStart, selEnd, clipboardText)
                requestFocus()
                setSelection(cursorFinalPosition)
            }
            else -> {
                val newText = StringBuilder(text).insert(selStart, clipboardText)
                val cursorFinalPosition = selStart + clipboardText.length
                setText(newText)
                requestFocus()
                setSelection(cursorFinalPosition)
            }
        }
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

    private fun updateText() {
        questionEditText.setText(viewModel.question)
        answerEditText.setText(viewModel.answer)
    }

    private fun executeCommand(command: EditCardController.Command) {
        when (command) {
            UpdateQuestionAndAnswer -> {
                updateText()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        requireActivity().currentFocus?.hideSoftInput()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            EditCardDiScope.close()
        }
    }
}