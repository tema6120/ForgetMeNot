package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.entry

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.firstBlocking
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import kotlinx.android.synthetic.main.fragment_answer_entry_test.*
import kotlinx.coroutines.flow.combine
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.scope.getViewModel
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope

class AnswerEntryTestFragment : BaseFragment() {
    companion object {
        private const val ARG_ID = "ARG_ID"

        fun create(id: Long) = AnswerEntryTestFragment().apply {
            arguments = Bundle(1).apply {
                putLong(ARG_ID, id)
            }
        }
    }

    private lateinit var viewModel: AnswerEntryTestViewModel
    private lateinit var controller: AnswerEntryTestController
    private lateinit var imm: InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = arguments!!.getLong(ARG_ID)
        val koinScope: Scope = getKoin()
            .getOrCreateScope<AnswerEntryTestViewModel>(ANSWER_ENTRY_TEST_SCOPE_ID_PREFIX + id)
        viewModel = koinScope.getViewModel(owner = this, parameters = { parametersOf(id) })
        controller = koinScope.get()
        imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_answer_entry_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
    }

    private fun setupView() {
        answerEditText.observeText { controller.onAnswerInputChanged(it?.toString()) }
        hintTextView.observeSelectedRange { startIndex: Int, endIndex: Int ->
            controller.onHintSelectionChanged(startIndex, endIndex)
        }
        checkButton.setOnClickListener { controller.onCheckButtonClicked() }
        correctAnswerTextView.observeSelectedText {
            controller.onAnswerTextSelectionChanged(it)
        }
        wrongAnswerTextView.run {
            observeSelectedText { controller.onAnswerTextSelectionChanged(it) }
            paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            isAnswered.observe { isAnswered: Boolean ->
                if (isAnswered) {
                    inputLayout.visibility = GONE
                    answerScrollView.visibility = VISIBLE
                } else {
                    inputLayout.visibility = VISIBLE
                    answerScrollView.visibility = GONE
                }
            }
            hint.observe { hint: String? ->
                if (hint == null) {
                    hintScrollView.visibility = GONE
                    divider.visibility = GONE
                } else {
                    hintTextView.text = hint
                    hintScrollView.visibility = VISIBLE
                    divider.visibility = VISIBLE
                }
            }
            correctAnswer.observe(correctAnswerTextView::setText)
            wrongAnswer.observe { wrongAnswer: String? ->
                if (wrongAnswer == null) {
                    wrongAnswerTextView.visibility = GONE
                } else {
                    wrongAnswerTextView.text = wrongAnswer
                    wrongAnswerTextView.visibility = VISIBLE
                }
            }
            isLearned.observe { isLearned: Boolean ->
                answerInputScrollView.isEnabled = !isLearned
                hintScrollView.isEnabled = !isLearned
                hintTextView.isEnabled = !isLearned
                checkButton.isEnabled = !isLearned
                checkTextView.isEnabled = !isLearned
                answerScrollView.isEnabled = !isLearned
                wrongAnswerTextView.isEnabled = !isLearned
                correctAnswerTextView.isEnabled = !isLearned
            }
            isAnswered.combine(isLearned) { isAnswered, isLearned -> !isAnswered && !isLearned }
                .observe(answerEditText::setEnabled)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!viewModel.isAnswered.firstBlocking()) {
            answerEditText.showSoftInput(showImplicit = true)
        }
    }
}