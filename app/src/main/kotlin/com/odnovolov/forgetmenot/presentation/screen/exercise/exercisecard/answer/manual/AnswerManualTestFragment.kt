package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.manual

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_answer_manual_test.*
import kotlinx.coroutines.flow.combine
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.scope.getViewModel
import org.koin.core.parameter.parametersOf

class AnswerManualTestFragment : BaseFragment() {
    companion object {
        private const val ARG_ID = "ARG_ID"

        fun create(id: Long) = AnswerManualTestFragment().apply {
            arguments = Bundle(1).apply {
                putLong(ARG_ID, id)
            }
        }
    }

    private lateinit var viewModel: AnswerManualTestViewModel
    private lateinit var controller: AnswerManualTestController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id: Long = arguments!!.getLong(ARG_ID)
        val koinScope = getKoin()
            .getOrCreateScope<AnswerManualTestViewModel>(ANSWER_MANUAL_TEST_SCOPE_ID_PREFIX + id)
        viewModel = koinScope.getViewModel(owner = this, parameters = { parametersOf(id) })
        controller = koinScope.get()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_answer_manual_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
    }

    private fun setupView() {
        answerTextView.observeSelectedText { controller.onAnswerTextSelectionChanged(it) }
        hintTextView.observeSelectedRange { startIndex: Int, endIndex: Int ->
            controller.onHintSelectionChanged(startIndex, endIndex)
        }
        rememberButton.setOnClickListener { controller.onRememberButtonClicked() }
        notRememberButton.setOnClickListener { controller.onNotRememberButtonClicked() }
    }

    private fun observeViewModel() {
        with(viewModel) {
            answer.observe(answerTextView::setText)
            isAnswerCorrect.observe { isAnswerCorrect: Boolean? ->
                isAnswerCorrect ?: return@observe
                rememberButton.isSelected = isAnswerCorrect
                notRememberButton.isSelected = !isAnswerCorrect
            }
            hint.observe(hintTextView::setText)
            isAnswerCorrect.combine(hint) { isAnswerCorrect, hint -> isAnswerCorrect to hint }
                .observe {
                    val (isAnswerCorrect: Boolean?, hint: String?) = it

                    val isAnswered = isAnswerCorrect != null
                    val hasHint = hint != null

                    when {
                        isAnswered -> {
                            curtainView.visibility = GONE
                            hintScrollView.visibility = GONE
                            answerScrollView.visibility = VISIBLE
                        }
                        hasHint -> {
                            curtainView.visibility = GONE
                            hintScrollView.visibility = VISIBLE
                            answerScrollView.visibility = GONE
                        }
                        else -> {
                            curtainView.visibility = VISIBLE
                            hintScrollView.visibility = GONE
                            answerScrollView.visibility = GONE
                        }
                    }
                }
            isLearned.observe { isLearned: Boolean ->
                answerScrollView.isEnabled = !isLearned
                answerTextView.isEnabled = !isLearned
                hintScrollView.isEnabled = !isLearned
                hintTextView.isEnabled = !isLearned
                rememberTextView.isEnabled = !isLearned
                rememberButton.isEnabled = !isLearned
                notRememberTextView.isEnabled = !isLearned
                notRememberButton.isEnabled = !isLearned
            }
        }
    }
}