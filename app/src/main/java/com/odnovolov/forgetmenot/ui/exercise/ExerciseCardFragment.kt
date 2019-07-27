package com.odnovolov.forgetmenot.ui.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.ui.exercise.ExerciseViewModel.Event.ShowAnswerButtonClick
import kotlinx.android.synthetic.main.fragment_exercise_card.*
import leakcanary.LeakSentry

class ExerciseCardFragment : Fragment() {

    companion object {
        private const val ARG_POSITION = "position"

        fun create(position: Int) =
            ExerciseCardFragment().apply {
                arguments = Bundle(1).apply {
                    putInt(ARG_POSITION, position)
                }
            }
    }

    lateinit var viewModel: ExerciseViewModel
    private var position: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = arguments!!.getInt(ARG_POSITION)
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
        subscribeToViewModel()
    }

    private fun setupView() {
        showAnswerButton.setOnClickListener { viewModel.onEvent(ShowAnswerButtonClick) }
    }

    private fun subscribeToViewModel() {
        with(viewModel.state) {
            exerciseCards.observe(viewLifecycleOwner, Observer { exerciseCards ->
                exerciseCards ?: return@Observer
                val exerciseCard = exerciseCards[position]

                val question = exerciseCard.card.question
                if (questionTextView.text.toString() != question) {
                    questionTextView.text = question
                }

                val answer = exerciseCard.card.answer
                if (answerTextView.text.toString() != answer) {
                    answerTextView.text = answer
                }

                if (exerciseCard.isAnswered) {
                    showAnswerButton.visibility = View.GONE
                    showAnswerButton.setOnClickListener(null)
                } else {
                    showAnswerButton.visibility = View.VISIBLE
                    showAnswerButton.setOnClickListener {
                        viewModel.onEvent(ShowAnswerButtonClick)
                    }
                }

                if (exerciseCard.card.isLearned) {
                    questionTextView.alpha = 0.26f
                    answerTextView.alpha = 0.26f
                } else {
                    questionTextView.alpha = 1f
                    answerTextView.alpha = 1f
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LeakSentry.refWatcher.watch(this)
    }
}