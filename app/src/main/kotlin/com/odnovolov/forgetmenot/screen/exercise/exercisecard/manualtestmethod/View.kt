package com.odnovolov.forgetmenot.screen.exercise.exercisecard.manualtestmethod

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.base.BaseFragment
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.manualtestmethod.ExerciseCardManualTestMethodEvent.*
import kotlinx.android.synthetic.main.fragment_exercise_card_manual_test_method.*
import leakcanary.LeakSentry

class ExerciseCardManualTestMethodFragment : BaseFragment() {
    companion object {
        private const val ARG_ID = "ARG_ID"

        fun create(id: Long) = ExerciseCardManualTestMethodFragment().apply {
            arguments = Bundle(1).apply {
                putLong(ARG_ID, id)
            }
        }
    }

    private lateinit var controller: ExerciseCardManualTestMethodController
    private lateinit var viewModel: ExerciseCardManualTestMethodViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = arguments!!.getLong(ARG_ID)
        controller = ExerciseCardManualTestMethodController(id)
        viewModel = ExerciseCardManualTestMethodViewModel(id)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater
            .inflate(R.layout.fragment_exercise_card_manual_test_method, container, false)
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
        rememberButton.setOnClickListener { controller.dispatch(RememberButtonClicked) }
        notRememberButton.setOnClickListener { controller.dispatch(NotRememberButtonClicked) }
    }

    private fun observeViewModel() {
        with(viewModel) {
            question.observe(onChange = questionTextView::setText)
            answer.observe(onChange = answerTextView::setText)
            isAnswerCorrect.observe { isAnswerCorrect: Boolean? ->
                isAnswerCorrect ?: return@observe
                curtainView.visibility = GONE
                if (isAnswerCorrect) {
                    val backgroundColor = ContextCompat
                        .getColor(requireContext(), R.color.background_correct_answer)
                    rememberButton.setBackgroundColor(backgroundColor)
                    notRememberButton.background = null
                } else {
                    val backgroundColor = ContextCompat
                        .getColor(requireContext(), R.color.background_wrong_answer)
                    notRememberButton.setBackgroundColor(backgroundColor)
                    rememberButton.background = null
                }
            }
            isLearned.observe { isLearned ->
                isLearned ?: return@observe
                questionTextView.setTextIsSelectable(!isLearned)
                answerTextView.setTextIsSelectable(!isLearned)
                showQuestionButton.isClickable = !isLearned
                rememberButton.isClickable = !isLearned
                notRememberButton.isClickable = !isLearned
                val alpha = if (isLearned) 0.26f else 1f
                showQuestionTextView.alpha = alpha
                questionTextView.alpha = alpha
                answerTextView.alpha = alpha
                rememberButton.alpha = alpha
                notRememberButton.alpha = alpha
            }
            isQuestionDisplayed.observe { isDisplayed ->
                showQuestionButton.run {
                    if (isDisplayed) {
                        visibility = GONE
                        setOnClickListener(null)
                    } else {
                        visibility = View.VISIBLE
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