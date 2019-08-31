package com.odnovolov.forgetmenot.exercise.exercisecards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.BaseFragment
import com.odnovolov.forgetmenot.exercise.exercisecards.ExerciseCardEvent.ShowAnswerButtonClicked
import kotlinx.android.synthetic.main.fragment_exercise_card.*
import leakcanary.LeakSentry

class ExerciseCardFragment : BaseFragment() {
    companion object {
        private const val ARG_CARD_ID = "ARG_CARD_ID"

        fun create(cardId: Long) = ExerciseCardFragment().apply {
            arguments = Bundle(1).apply {
                putLong(ARG_CARD_ID, cardId)
            }
        }
    }

    private lateinit var controller: ExerciseCardController
    private lateinit var viewModel: ExerciseCardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val cardId = arguments!!.getLong(ARG_CARD_ID)
        controller = ExerciseCardController(cardId)
        viewModel = ExerciseCardViewModel(cardId)
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
                if (isLearned) {
                    questionTextView.alpha = 0.26f
                    answerTextView.alpha = 0.26f
                } else {
                    questionTextView.alpha = 1f
                    answerTextView.alpha = 1f
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