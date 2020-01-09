package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.off

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.base.BaseFragment
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.off.AnswerOffTestEvent.*
import kotlinx.android.synthetic.main.fragment_answer_off_test.*

class AnswerOffTestFragment : BaseFragment() {
    companion object {
        private const val ARG_ID = "ARG_ID"

        fun create(id: Long) = AnswerOffTestFragment().apply {
            arguments = Bundle(1).apply {
                putLong(ARG_ID, id)
            }
        }
    }

    private lateinit var controller: AnswerOffTestController
    private lateinit var viewModel: AnswerOffTestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = arguments!!.getLong(ARG_ID)
        controller = AnswerOffTestController(id)
        viewModel = AnswerOffTestViewModel(id)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_answer_off_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
    }

    private fun setupView() {
        answerTextView.observeSelectedText { controller.dispatch(AnswerTextSelectionChanged(it)) }
        showAnswerButton.setOnClickListener { controller.dispatch(ShowAnswerButtonClicked) }
    }

    private fun observeViewModel() {
        with(viewModel) {
            answer.observe(onChange = answerTextView::setText)
            isAnswered.observe { isAnswered: Boolean ->
                showAnswerButton.visibility = if (isAnswered) GONE else VISIBLE
            }
            isLearned.observe { isLearned: Boolean ->
                answerScrollView.isEnabled = !isLearned
                answerTextView.isEnabled = !isLearned
                showAnswerButton.isEnabled = !isLearned
                showAnswerTextView.isEnabled = !isLearned
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
    }
}