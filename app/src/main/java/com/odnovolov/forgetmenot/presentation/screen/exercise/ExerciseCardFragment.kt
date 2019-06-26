package com.odnovolov.forgetmenot.presentation.screen.exercise

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.ExerciseCard
import com.odnovolov.forgetmenot.presentation.common.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mvicorediff.modelWatcher
import com.odnovolov.forgetmenot.presentation.di.Injector
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreen.UiEvent
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreen.UiEvent.ShowAnswerButtonClick
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreen.ViewState
import kotlinx.android.synthetic.main.fragment_exercise_card.*
import javax.inject.Inject

class ExerciseCardFragment : BaseFragment<ViewState, UiEvent, Nothing>() {

    companion object {
        private const val KEY_EXERCISE_CARD_ID =
            "com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseCardFragment.KEY_EXERCISE_CARD_ID"

        fun create(exerciseCardId: Int) =
            ExerciseCardFragment().apply {
                arguments = Bundle(1).apply {
                    putInt(KEY_EXERCISE_CARD_ID, exerciseCardId)
                }
            }
    }

    @Inject lateinit var bindings: ExerciseCardFragmentBindings
    private var exerciseCardId: Int = -1

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Injector.inject(this)
        bindings.setup(this)
        exerciseCardId = arguments?.getInt(KEY_EXERCISE_CARD_ID) ?: throw IllegalStateException()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_exercise_card, container, false)
    }

    override fun accept(viewState: ViewState) {
        val exerciseCard = viewState.exerciseCards.find { exerciseCard -> exerciseCard.id == exerciseCardId }
        watcher.invoke(exerciseCard!!)
    }

    private val watcher = modelWatcher<ExerciseCard> {
        watch({ it.card.question }) { question ->
            questionTextView.text = question
        }
        watch({ it.card.answer }) { answer ->
            answerTextView.text = answer
        }
        watch({ it.isAnswered }) { isAnswered ->
            if (isAnswered) {
                showAnswerButton.visibility = View.GONE
                showAnswerButton.setOnClickListener(null)
            } else {
                showAnswerButton.visibility = View.VISIBLE
                showAnswerButton.setOnClickListener {
                    emitEvent(ShowAnswerButtonClick)
                }
            }
        }
        watch({it.card.isLearned}) { isLearned ->
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