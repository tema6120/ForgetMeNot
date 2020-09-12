package com.odnovolov.forgetmenot.domain.interactor.exercise

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker

class ManualTestExerciseCard(
    base: ExerciseCard.Base
) : FlowMaker<ManualTestExerciseCard>(), ExerciseCard {
    override val base: ExerciseCard.Base by flowMaker(base)
}