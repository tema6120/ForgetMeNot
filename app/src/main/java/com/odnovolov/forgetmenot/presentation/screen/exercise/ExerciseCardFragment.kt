package com.odnovolov.forgetmenot.presentation.screen.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mvicorediff.modelWatcher
import com.odnovolov.forgetmenot.presentation.entity.ExerciseCardViewEntity
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreenFeature.UiEvent
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreenFeature.UiEvent.ShowAnswerButtonClick
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreenFeature.ViewState
import com.odnovolov.forgetmenot.presentation.screen.exercise.di.ExerciseScreenComponent
import kotlinx.android.synthetic.main.fragment_exercise_card.*
import leakcanary.LeakSentry
import javax.inject.Inject

class ExerciseCardFragment : BaseFragment<ViewState, UiEvent, Nothing>() {

    companion object {
        private const val KEY_POSITION =
            "com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseCardFragment.KEY_POSITION"

        fun create(position: Int) =
            ExerciseCardFragment().apply {
                arguments = Bundle(1).apply {
                    putInt(KEY_POSITION, position)
                }
            }
    }

    @Inject lateinit var bindings: ExerciseCardFragmentBindings
    private var position: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        ExerciseScreenComponent.get()!!.inject(this)
        super.onCreate(savedInstanceState)
        position = arguments?.getInt(KEY_POSITION) ?: throw IllegalStateException()
        bindings.setup(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_exercise_card, container, false)
    }

    override fun accept(viewState: ViewState) {
        val exerciseCardViewEntity = viewState.exerciseCards[position]
        watcher.invoke(exerciseCardViewEntity)
    }

    private val watcher = modelWatcher<ExerciseCardViewEntity> {
        watch(
            accessor = { exerciseCardViewEntity -> exerciseCardViewEntity.cardViewEntity.question },
            callback = { question -> questionTextView.text = question }
        )
        watch(
            accessor = { exerciseCardViewEntity -> exerciseCardViewEntity.cardViewEntity.answer },
            callback = { answer -> answerTextView.text = answer }
        )
        watch(
            accessor = ExerciseCardViewEntity::isAnswered,
            callback = { isAnswered ->
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
        )
        watch(
            accessor = { exerciseCardViewEntity -> exerciseCardViewEntity.cardViewEntity.isLearned },
            callback = { isLearned ->
                if (isLearned) {
                    questionTextView.alpha = 0.26f
                    answerTextView.alpha = 0.26f
                } else {
                    questionTextView.alpha = 1f
                    answerTextView.alpha = 1f
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        LeakSentry.refWatcher.watch(this)
    }
}