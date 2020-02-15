package com.odnovolov.forgetmenot.domain.interactor.exercise

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.domain.entity.Card

class QuizTestExerciseCard(
    base: ExerciseCard.Base,
    variants: List<Card?>,
    selectedVariantIndex: Int? = null
) : FlowableState<QuizTestExerciseCard>(), ExerciseCard {
    override val base: ExerciseCard.Base by me(base)
    val variants: List<Card?> by me(variants)
    var selectedVariantIndex: Int? by me(selectedVariantIndex)
}