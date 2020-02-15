package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.off

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.LinearLayout
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.dp
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_answer_off_test.*
import kotlinx.coroutines.flow.combine
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.scope.getViewModel
import org.koin.core.parameter.parametersOf

class AnswerOffTestFragment : BaseFragment() {
    companion object {
        private const val ARG_ID = "ARG_ID"

        fun create(id: Long) = AnswerOffTestFragment().apply {
            arguments = Bundle(1).apply {
                putLong(ARG_ID, id)
            }
        }
    }

    private lateinit var viewModel: AnswerOffTestViewModel
    private lateinit var controller: AnswerOffTestController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id: Long = arguments!!.getLong(ARG_ID)
        val koinScope = getKoin()
            .getOrCreateScope<AnswerOffTestViewModel>(ANSWER_OFF_TEST_SCOPE_ID_PREFIX + id)
        viewModel = koinScope.getViewModel(owner = this, parameters = { parametersOf(id) })
        controller = koinScope.get()
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
        answerTextView.observeSelectedText { controller.onAnswerTextSelectionChanged(it) }
        hintTextView.observeSelectedRange { startIndex: Int, endIndex: Int ->
            controller.onHintSelectionChanged(startIndex, endIndex)
        }
        showAnswerButton.setOnClickListener { controller.onShowAnswerButtonClicked() }
    }

    private fun observeViewModel() {
        with(viewModel) {
            answer.observe(answerTextView::setText)
            hint.observe(hintTextView::setText)
            isAnswered.combine(hint) { isAnswered, hint -> isAnswered to hint }
                .observe {
                    val (isAnswered, hint) = it
                    val hasHint = hint != null
                    when {
                        isAnswered -> {
                            answerScrollView.visibility = VISIBLE
                            hintScrollView.visibility = GONE
                            divider.visibility = GONE
                            showAnswerButton.visibility = GONE
                        }
                        hasHint -> {
                            answerScrollView.visibility = GONE
                            hintScrollView.visibility = VISIBLE
                            divider.visibility = VISIBLE
                            with(showAnswerButton) {
                                visibility = VISIBLE
                                layoutParams.height = 50.dp
                                requestLayout()
                            }
                        }
                        else -> {
                            answerScrollView.visibility = GONE
                            hintScrollView.visibility = GONE
                            divider.visibility = GONE
                            with(showAnswerButton) {
                                visibility = VISIBLE
                                layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT
                                requestLayout()
                            }
                        }
                    }
                }
            isLearned.observe { isLearned: Boolean ->
                answerScrollView.isEnabled = !isLearned
                answerTextView.isEnabled = !isLearned
                hintScrollView.isEnabled = !isLearned
                hintTextView.isEnabled = !isLearned
                showAnswerButton.isEnabled = !isLearned
                showAnswerTextView.isEnabled = !isLearned
            }
        }
    }
}