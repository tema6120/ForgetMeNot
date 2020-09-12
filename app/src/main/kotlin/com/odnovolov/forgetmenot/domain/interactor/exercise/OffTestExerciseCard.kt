package com.odnovolov.forgetmenot.domain.interactor.exercise

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker

class OffTestExerciseCard(
    base: ExerciseCard.Base
) : FlowMaker<OffTestExerciseCard>(), ExerciseCard {
    override val base: ExerciseCard.Base by flowMaker(base)
}