package com.odnovolov.forgetmenot.screen.exercise.exercisecard.withouttest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.base.BaseFragment
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.withouttest.ExerciseCardWithoutTestEvent.*
import kotlinx.android.synthetic.main.fragment_exercise_card_without_test.*
import leakcanary.LeakSentry

class ExerciseCardWithoutTestFragment : BaseFragment() {
    companion object {
        private const val ARG_ID = "ARG_ID"

        fun create(id: Long) = ExerciseCardWithoutTestFragment().apply {
            arguments = Bundle(1).apply {
                putLong(ARG_ID, id)
            }
        }
    }

    private lateinit var controller: ExerciseCardWithoutTestController
    private lateinit var viewModel: ExerciseCardWithoutTextViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = arguments!!.getLong(ARG_ID)
        controller = ExerciseCardWithoutTestController(id)
        viewModel = ExerciseCardWithoutTextViewModel(id)
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
        questionTextView.observeSelectedText {
            controller.dispatch(QuestionTextSelectionChanged(it))
        }
        answerTextView.observeSelectedText { controller.dispatch(AnswerTextSelectionChanged(it)) }
        showAnswerButton.setOnClickListener { controller.dispatch(ShowAnswerButtonClicked) }
    }

    private fun observeViewModel() {
        with(viewModel) {
            question.observe(onChange = questionTextView::setText)
            answer.observe(onChange = answerTextView::setText)
            isAnswered.observe { isAnswered ->
                showAnswerButton.run {
                    if (isAnswered) {
                        visibility = GONE
                        setOnClickListener(null)
                    } else {
                        visibility = VISIBLE
                        setOnClickListener { controller.dispatch(ShowAnswerButtonClicked) }
                    }
                }
            }
            isLearned.observe { isLearned ->
                isLearned ?: return@observe
                questionTextView.setTextIsSelectable(!isLearned)
                answerTextView.setTextIsSelectable(!isLearned)
                showQuestionButton.isClickable = !isLearned
                showAnswerButton.isClickable = !isLearned
                val alpha = if (isLearned) 0.26f else 1f
                showQuestionTextView.alpha = alpha
                showAnswerTextView.alpha = alpha
                questionTextView.alpha = alpha
                answerTextView.alpha = alpha
            }
            isQuestionDisplayed.observe { isDisplayed ->
                showQuestionButton.run {
                    if (isDisplayed) {
                        visibility = GONE
                        setOnClickListener(null)
                    } else {
                        visibility = VISIBLE
                        setOnClickListener { controller.dispatch(ShowQuestionButtonClicked) }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
        LeakSentry.refWatcher.watch(this)
    }
}