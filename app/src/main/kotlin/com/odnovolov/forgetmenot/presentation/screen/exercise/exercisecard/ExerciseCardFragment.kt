package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.TestMethod
import com.odnovolov.forgetmenot.domain.entity.TestMethod.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.entry.AnswerEntryTestFragment
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.manual.AnswerManualTestFragment
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.off.AnswerOffTestFragment
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.quiz.AnswerQuizTestFragment
import kotlinx.android.synthetic.main.fragment_exercise_card.*
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.scope.getViewModel
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope

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

    private var id: Long? = null
    private lateinit var viewModel: ExerciseCardViewModel
    private lateinit var controller: ExerciseCardController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        id = arguments!!.getLong(ARG_ID)
        val koinScope: Scope = getKoin()
            .getOrCreateScope<ExerciseCardViewModel>(EXERCISE_CARD_SCOPE_ID_PREFIX + id)
        viewModel = koinScope.getViewModel(owner = this, parameters = { parametersOf(id) })
        controller = koinScope.get()
    }

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
        showQuestionButton.setOnClickListener { controller.onShowQuestionButtonClicked() }
        questionTextView.observeSelectedText { controller.onQuestionTextSelectionChanged(it) }
    }

    private fun observeViewModel() {
        with(viewModel) {
            testMethod.observe { testMethod: TestMethod ->
                val currentFragment: Fragment? = childFragmentManager
                    .findFragmentByTag(ANSWER_FRAGMENT_TAG)
                val newFragment: Fragment? = when (testMethod) {
                    Off -> {
                        if (currentFragment is AnswerOffTestFragment) null
                        else AnswerOffTestFragment.create(id!!)
                    }
                    Manual -> {
                        if (currentFragment is AnswerManualTestFragment) null
                        else AnswerManualTestFragment.create(id!!)
                    }
                    Quiz -> {
                        if (currentFragment is AnswerQuizTestFragment) null
                        else AnswerQuizTestFragment.create(id!!)
                    }
                    Entry -> {
                        if (currentFragment is AnswerEntryTestFragment) null
                        else AnswerEntryTestFragment.create(id!!)
                    }
                }
                if (newFragment != null) {
                    childFragmentManager
                        .beginTransaction()
                        .replace(R.id.answerFrame, newFragment, ANSWER_FRAGMENT_TAG)
                        .commit()
                }
            }
            question.observe(questionTextView::setText)
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
}