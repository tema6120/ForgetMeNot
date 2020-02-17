package com.odnovolov.forgetmenot.presentation.screen.editcard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.observeText
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.editcard.EditCardCommand.UpdateQuestionAndAnswer
import kotlinx.android.synthetic.main.fragment_edit_card.*
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.scope.viewModel

class EditCardFragment : BaseFragment() {
    private val koinScope = getKoin().getOrCreateScope<EditCardViewModel>(EDIT_CARD_SCOPE_ID)
    private val viewModel: EditCardViewModel by koinScope.viewModel(this)
    private val controller: EditCardController by koinScope.inject()

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
        if (savedInstanceState == null) {
            updateText()
        }
        viewModel.isDoneButtonEnabled.observe(doneButton::setEnabled)
        controller.commands.observe(::executeCommand)
    }

    private fun setupView() {
        questionEditText.observeText { controller.onQuestionInputChanged(it) }
        answerEditText.observeText { controller.onAnswerInputChanged(it) }
        reverseCardButton.setOnClickListener { controller.onReverseCardButtonClicked() }
        cancelButton.setOnClickListener { controller.onCancelButtonClicked() }
        doneButton.setOnClickListener { controller.onDoneButtonClicked() }
    }

    private fun updateText() {
        questionEditText.setText(viewModel.question)
        answerEditText.setText(viewModel.answer)
    }

    private fun executeCommand(command: EditCardCommand) {
        when (command) {
            UpdateQuestionAndAnswer -> {
                updateText()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        hideSoftKeyboard()
    }

    private fun hideSoftKeyboard() {
        with(requireActivity()) {
            val focusedView = currentFocus ?: return
            focusedView.clearFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
        }
    }
}