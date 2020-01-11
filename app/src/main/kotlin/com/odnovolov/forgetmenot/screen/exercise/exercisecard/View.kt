package com.odnovolov.forgetmenot.screen.exercise.exercisecard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.base.BaseFragment
import com.odnovolov.forgetmenot.common.entity.TestMethod
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
        private const val ANSWER_FRAGMENT_TAG = "ANSWER_FRAGMENT_TAG"

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
        return inflater.inflate(R.layout.fragment_exercise_card, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
    }

    private fun setupView() {
        showQuestionButton.setOnClickListener { controller.dispatch(ShowQuestionButtonClicked) }
        questionTextView.observeSelectedText {
            controller.dispatch(QuestionTextSelectionChanged(it))
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            testMethod.observe { testMethod: TestMethod ->
                val currentFragment: Fragment? = childFragmentManager
                    .findFragmentByTag(ANSWER_FRAGMENT_TAG)
                val newFragment: Fragment? = when (testMethod) {
                    Off -> {
                        if (currentFragment is AnswerOffTestFragment) null
                        else AnswerOffTestFragment.create(id)
                    }
                    Manual -> {
                        if (currentFragment is AnswerManualTestFragment) null
                        else AnswerManualTestFragment.create(id)
                    }
                    Quiz -> {
                        if (currentFragment is AnswerQuizTestFragment) null
                        else AnswerQuizTestFragment.create(id)
                    }
                    Entry -> {
                        if (currentFragment is AnswerEntryTestFragment) null
                        else AnswerEntryTestFragment.create(id)
                    }
                }
                if (newFragment != null) {
                    childFragmentManager
                        .beginTransaction()
                        .replace(R.id.answerFrame, newFragment, ANSWER_FRAGMENT_TAG)
                        .commit()
                }
            }
            question.observe(onChange = questionTextView::setText)
            isQuestionDisplayed.observe { isDisplayed: Boolean ->
                showQuestionButton.visibility = if (isDisplayed) GONE else VISIBLE
            }
            isLearned.observe { isLearned: Boolean ->
                questionScrollView.isEnabled = !isLearned
                questionTextView.isEnabled = !isLearned
                showQuestionButton.isEnabled = !isLearned
                showQuestionTextView.isEnabled = !isLearned
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
    }
}