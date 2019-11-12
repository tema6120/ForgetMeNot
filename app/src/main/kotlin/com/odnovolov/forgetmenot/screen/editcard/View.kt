package com.odnovolov.forgetmenot.screen.editcard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.navigation.fragment.findNavController
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.base.BaseFragment
import com.odnovolov.forgetmenot.common.observeText
import com.odnovolov.forgetmenot.screen.editcard.EditCardEvent.*
import com.odnovolov.forgetmenot.screen.editcard.EditCardOrder.NavigateUp
import com.odnovolov.forgetmenot.screen.editcard.EditCardOrder.UpdateQuestionAndAnswer
import kotlinx.android.synthetic.main.fragment_edit_card.*
import leakcanary.LeakSentry

class EditCardFragment : BaseFragment() {
    private val controller = EditCardController()

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
        controller.orders.forEach(viewScope!!, ::executeOrder)
    }

    private fun setupView() {
        questionEditTextView.observeText { controller.dispatch(QuestionInputChanged(it)) }
        answerEditTextView.observeText { controller.dispatch(AnswerInputChanged(it)) }
        reverseCardButton.setOnClickListener { controller.dispatch(ReverseCardButtonClicked) }
        cancelButton.setOnClickListener { controller.dispatch(CancelButtonClicked) }
        doneButton.setOnClickListener { controller.dispatch(DoneButtonClicked) }
    }

    private fun updateText() {
        with(EditCardViewModel()) {
            questionEditTextView.setText(question)
            answerEditTextView.setText(answer)
        }
    }

    private fun executeOrder(order: EditCardOrder) {
        when (order) {
            UpdateQuestionAndAnswer -> {
                updateText()
            }
            NavigateUp -> {
                hideSoftKeyboard()
                findNavController().navigateUp()
            }
        }
    }

    private fun hideSoftKeyboard() {
        with(requireActivity()) {
            val focusedView = currentFocus ?: return
            focusedView.clearFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(focusedView.windowToken, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
        LeakSentry.refWatcher.watch(this)
    }
}