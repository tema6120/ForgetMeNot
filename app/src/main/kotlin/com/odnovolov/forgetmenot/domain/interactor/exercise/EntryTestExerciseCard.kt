package com.odnovolov.forgetmenot.domain.interactor.exercise

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker

class EntryTestExerciseCard(
    base: ExerciseCard.Base,
    userInput: String? = null
) : FlowMaker<EntryTestExerciseCard>(), ExerciseCard {
    override val base: ExerciseCard.Base by flowMaker(base)
    var userInput: String? by flowMaker(userInput)
}