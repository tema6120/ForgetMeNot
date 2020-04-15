package com.odnovolov.forgetmenot.presentation.screen.editcard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.hideSoftInput
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.screen.editcard.EditCardController.Command.UpdateQuestionAndAnswer
import com.odnovolov.forgetmenot.presentation.screen.editcard.EditCardEvent.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseDiScope
import kotlinx.android.synthetic.main.fragment_edit_card.*
import kotlinx.coroutines.launch

class EditCardFragment : BaseFragment() {
    init {
        ExerciseDiScope.reopenIfClosed()
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