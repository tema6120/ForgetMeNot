package com.odnovolov.forgetmenot.domain.interactor.exercise

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState

class EntryTestExerciseCard(
    base: ExerciseCard.Base,
    userAnswer: String? = null
) : FlowableState<EntryTestExerciseCard>(), ExerciseCard {
    override val base: ExerciseCard.Base by me(base)
    var userAnswer: String? by me(userAnswer)
}