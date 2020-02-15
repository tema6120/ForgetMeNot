package com.odnovolov.forgetmenot.domain.interactor.exercise

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState

class ManualTestExerciseCard(
    base: ExerciseCard.Base
) : FlowableState<ManualTestExerciseCard>(), ExerciseCard {
    override val base: ExerciseCard.Base by me(base)
}