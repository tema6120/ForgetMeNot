package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.manual

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.base.BaseFragment
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.manual.AnswerManualTestEvent.*
import kotlinx.android.synthetic.main.fragment_answer_manual_test.*
import leakcanary.LeakSentry

class AnswerManualTestFragment : BaseFragment() {
    companion object {
        private const val ARG_ID = "ARG_ID"

        fun create(id: Long) = AnswerManualTestFragment().apply {
            arguments = Bundle(1).apply {
                putLong(ARG_ID, id)
            }
        }
    }

    private lateinit var controller: AnswerManualTestController
    private lateinit var viewModel: AnswerManualTestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = arguments!!.getLong(ARG_ID)
        controller = AnswerManualTestController(id)
        viewModel = AnswerManualTestViewModel(id)
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
        answerTextView.observeSelectedText { controller.dispatch(AnswerTextSelectionChanged(it)) }
        rememberButton.setOnClickListener { controller.dispatch(RememberButtonClicked) }
        notRememberButton.setOnClickListener { controller.dispatch(NotRememberButtonClicked) }
    }

    private fun observeViewModel() {
        with(viewModel) {
            answer.observe(onChange = answerTextView::setText)
            isAnswerCorrect.observe { isAnswerCorrect: Boolean? ->
                isAnswerCorrect ?: return@observe
                curtainView.visibility = GONE
                if (isAnswerCorrect) {
                    val backgroundColor = ContextCompat
                        .getColor(requireContext(), R.color.correct_answer)
                    rememberButton.setBackgroundColor(backgroundColor)
                    notRememberButton.background = null
                } else {
                    val backgroundColor = ContextCompat
                        .getColor(requireContext(), R.color.wrong_answer)
                    notRememberButton.setBackgroundColor(backgroundColor)
                    rememberButton.background = null
                }
            }
            isLearned.observe { isLearned ->
                answerTextView.setTextIsSelectable(!isLearned)
                rememberButton.isClickable = !isLearned
                notRememberButton.isClickable = !isLearned
                val alpha = if (isLearned) 0.26f else 1f
                answerTextView.alpha = alpha
                rememberButton.alpha = alpha
                notRememberButton.alpha = alpha
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
        LeakSentry.refWatcher.watch(this)
    }
}