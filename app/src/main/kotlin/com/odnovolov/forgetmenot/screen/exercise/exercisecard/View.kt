package com.odnovolov.forgetmenot.screen.exercise.exercisecard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.base.BaseFragment
import com.odnovolov.forgetmenot.common.entity.TestMethod.*
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.ExerciseCardEvent.QuestionTextSelectionChanged
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.ExerciseCardEvent.ShowQuestionButtonClicked
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.entry.AnswerEntryTestFragment
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.manual.AnswerManualTestFragment
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.off.AnswerOffTestFragment
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.quiz.AnswerQuizTestFragment
import kotlinx.android.synthetic.main.fragment_exercise_card.*

class ExerciseCardFragment : BaseFragment() {
    companion object {
        private const val ARG_ID = "ARG_ID"

        fun create(id: Long) = ExerciseCardFragment().apply {
            arguments = Bundle(1).apply {
                putLong(ARG_ID, id)
            }
        }
    }

    private val id by lazy { arguments!!.getLong(ARG_ID) }
    private val viewModel by lazy { ExerciseCardViewModel(id) }
    private val controller by lazy { ExerciseCardController(id) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (savedInstanceState == null) {
            val answerFragment = when (viewModel.testMethod) {
                Off -> AnswerOffTestFragment.create(id)
                Manual -> AnswerManualTestFragment.create(id)
                Quiz -> AnswerQuizTestFragment.create(id)
                Entry -> AnswerEntryTestFragment.create(id)
            }
            childFragmentManager
                .beginTransaction()
                .replace(R.id.answerFrame, answerFragment, "tag")
                .commit()
        }
        return inflater.inflate(R.layout.fragment_exercise_card, container, false)
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
    }

    private fun observeViewModel() {
        with(viewModel) {
            question.observe(onChange = questionTextView::setText)
            isQuestionDisplayed.observe { isDisplayed ->
                showQuestionButton.run {
                    if (isDisplayed) {
                        visibility = View.GONE
                        setOnClickListener(null)
                    } else {
                        visibility = View.VISIBLE
                        setOnClickListener { controller.dispatch(ShowQuestionButtonClicked) }
                    }
                }
            }
            isLearned.observe { isLearned ->
                isLearned ?: return@observe
                questionTextView.setTextIsSelectable(!isLearned)
                showQuestionButton.isClickable = !isLearned
                val alpha = if (isLearned) 0.26f else 1f
                questionTextView.alpha = alpha
                showQuestionTextView.alpha = alpha
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
    }
}