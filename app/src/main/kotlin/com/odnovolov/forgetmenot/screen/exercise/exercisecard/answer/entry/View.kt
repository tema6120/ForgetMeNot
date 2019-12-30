package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.entry

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.base.BaseFragment
import com.odnovolov.forgetmenot.common.observeText
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.entry.AnswerEntryTestEvent.*
import kotlinx.android.synthetic.main.fragment_answer_entry_test.*

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
        answerEditText.observeText { controller.dispatch(AnswerInputChanged(it)) }
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
            isAnswered.observe { isAnswered: Boolean? ->
                if (isAnswered == true) {
                    inputLayout.visibility = GONE
                    answerScrollView.visibility = VISIBLE
                } else {
                    inputLayout.visibility = VISIBLE
                    answerScrollView.visibility = GONE
                }
                answerEditText.isEnabled = isAnswered != true
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
            answerEditText.setText("")
            isLearned.observe { isLearned: Boolean? ->
                val isViewEnable = isLearned == false
                answerEditText.isEnabled = isViewEnable
                checkButton.isEnabled = isViewEnable
                correctAnswerTextView.isEnabled = isViewEnable
                wrongAnswerTextView.isEnabled = isViewEnable

                val alpha = if (isLearned == true) 0.26f else 1f
                answerEditText.alpha = alpha
                checkButton.alpha = alpha
                wrongAnswerTextView.alpha = alpha
                correctAnswerTextView.alpha = alpha
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
    }
}