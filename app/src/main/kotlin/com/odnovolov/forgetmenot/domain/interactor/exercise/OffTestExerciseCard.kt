package com.odnovolov.forgetmenot.domain.interactor.exercise

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState

class OffTestExerciseCard(
    base: ExerciseCard.Base
) : FlowableState<OffTestExerciseCard>(), ExerciseCard {
    override val base: ExerciseCard.Base by me(base)
}