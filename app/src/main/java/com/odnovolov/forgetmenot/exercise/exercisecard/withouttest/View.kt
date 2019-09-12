package com.odnovolov.forgetmenot.exercise.exercisecard.withouttest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.base.BaseFragment
import com.odnovolov.forgetmenot.exercise.exercisecard.withouttest.ExerciseCardWithoutTestEvent.ShowAnswerButtonClicked
import kotlinx.android.synthetic.main.fragment_exercise_card_without_test.*
import leakcanary.LeakSentry

class ExerciseCardWithoutTestFragment : BaseFragment() {
    companion object {
        private const val ARG_CARD_ID = "ARG_CARD_ID"

        fun create(cardId: Long) = ExerciseCardWithoutTestFragment().apply {
            arguments = Bundle(1).apply {
                putLong(ARG_CARD_ID, cardId)
            }
        }
    }

    private lateinit var controller: ExerciseCardWithoutTestController
    private lateinit var viewModel: ExerciseCardWithoutTextViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cardId = arguments!!.getLong(ARG_CARD_ID)
        controller = ExerciseCardWithoutTestController(cardId)
        viewModel = ExerciseCardWithoutTextViewModel(cardId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_exercise_card_without_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
    }

    private fun setupView() {
        showAnswerButton.setOnClickListener { controller.dispatch(ShowAnswerButtonClicked) }
    }

    private fun observeViewModel() {
        with(viewModel) {
            question.observe(onChange = questionTextView::setText)
            answer.observe(onChange = answerTextView::setText)
            isAnswered.observe { isAnswered ->
                if (isAnswered) {
                    showAnswerButton.visibility = View.GONE
                    showAnswerButton.setOnClickListener(null)
                } else {
                    showAnswerButton.visibility = View.VISIBLE
                    showAnswerButton.setOnClickListener {
                        controller.dispatch(ShowAnswerButtonClicked)
                    }
                }
            }
            isLearned.observe { isLearned ->
                showAnswerButton.isClickable = !isLearned
                val alpha = if (isLearned) 0.26f else 1f
                questionTextView.alpha = alpha
                answerTextView.alpha = alpha
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
        LeakSentry.refWatcher.watch(this)
    }
}