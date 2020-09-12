package com.odnovolov.forgetmenot.domain.interactor.exercise

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.Card

class QuizTestExerciseCard(
    base: ExerciseCard.Base,
    variants: List<Card?>,
    selectedVariantIndex: Int? = null
) : FlowMaker<QuizTestExerciseCard>(), ExerciseCard {
    override val base: ExerciseCard.Base by flowMaker(base)
    val variants: List<Card?> by flowMaker(variants)
    var selectedVariantIndex: Int? by flowMaker(selectedVariantIndex)
}