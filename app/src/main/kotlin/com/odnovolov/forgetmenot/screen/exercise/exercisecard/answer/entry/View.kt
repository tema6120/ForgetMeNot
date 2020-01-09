package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.entry

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
import com.odnovolov.forgetmenot.common.base.BaseFragment
import com.odnovolov.forgetmenot.common.observeText
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.entry.AnswerEntryTestEvent.*
import kotlinx.android.synthetic.main.fragment_answer_entry_test.*
import kotlinx.coroutines.flow.combine

class AnswerEntryTestFragment : BaseFragment() {
    companion object {
        private const val ARG_ID = "ARG_ID"

        fun create(id: Long) = AnswerEntryTestFragment().apply {
            arguments = Bundle(1).apply {
                putLong(ARG_ID, id)
            }
        }
    }

    private lateinit var controller: AnswerEntryTestController
    private lateinit var viewModel: AnswerEntryTestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = arguments!!.getLong(ARG_ID)
        controller = AnswerEntryTestController(id)
        viewModel = AnswerEntryTestViewModel(id)
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
        answerEditText.run {
            observeText { controller.dispatch(AnswerInputChanged(it)) }
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    val imm = requireContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.showSoftInput(answerEditText, 0)
                }
            }
        }
        checkButton.setOnClickListener { controller.dispatch(CheckButtonClicked) }
        correctAnswerTextView.observeSelectedText {
            controller.dispatch(AnswerTextSelectionChanged(it))
        }
        wrongAnswerTextView.run {
            observeSelectedText { controller.dispatch(AnswerTextSelectionChanged(it)) }
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
            correctAnswer.observe(onChange = correctAnswerTextView::setText)
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
                checkButton.isEnabled = !isLearned
                checkTextView.isEnabled = !isLearned
                answerScrollView.isEnabled = !isLearned
                wrongAnswerTextView.isEnabled = !isLearned
                correctAnswerTextView.isEnabled = !isLearned
            }
            isAnswered.combine(isLearned) { isAnswered, isLearned -> !isAnswered && !isLearned }
                .observe(onChange = answerEditText::setEnabled)
        }
    }

    override fun onResume() {
        super.onResume()
        answerEditText.requestFocus()
        answerEditText.postDelayed({ answerEditText.requestFocus() }, 100)
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
    }
}