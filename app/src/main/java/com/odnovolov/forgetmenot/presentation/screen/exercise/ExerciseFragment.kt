package com.odnovolov.forgetmenot.presentation.screen.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseCard
import com.odnovolov.forgetmenot.presentation.common.UiEventEmitterFragment
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseFragment.UiEvent
import kotlinx.android.synthetic.main.fragment_exercise.*

class ExerciseFragment : UiEventEmitterFragment<UiEvent>() {

    sealed class UiEvent {
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_exercise, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = ExerciseCardsAdapter()
        exerciseRecycler.adapter = adapter
        val exerciseCards = listOf<ExerciseCard>(
            ExerciseCard(0, Card(0, 1, "question_1", "answer_1"), false),
            ExerciseCard(1, Card(1, 1, "question_2", "answer_2"), false)
        )
        adapter.submitList(exerciseCards)
    }
}