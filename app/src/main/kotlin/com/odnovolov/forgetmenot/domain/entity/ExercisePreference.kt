package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.Copyable
import com.odnovolov.forgetmenot.domain.architecturecomponents.RegistrableFlowableState

class ExercisePreference(
    override val id: Long = 0L,
    name: String = "",
    randomOrder: Boolean = true,
    testMethod: TestMethod = TestMethod.Manual,
    intervalScheme: IntervalScheme? = IntervalScheme.Default,
    pronunciation: Pronunciation = Pronunciation.Default,
    isQuestionDisplayed: Boolean = true,
    cardReverse: CardReverse = CardReverse.Off
) : RegistrableFlowableState<ExercisePreference>(), Copyable {
    var name: String by me(name)
    var randomOrder: Boolean by me(randomOrder)
    var testMethod: TestMethod by me(testMethod)
    var intervalScheme: IntervalScheme? by me(intervalScheme)
    var pronunciation: Pronunciation by me(pronunciation)
    var isQuestionDisplayed: Boolean by me(isQuestionDisplayed)
    var cardReverse: CardReverse by me(cardReverse)

    override fun copy() = ExercisePreference(
        id,
        name,
        randomOrder,
        testMethod,
        intervalScheme?.copy(),
        pronunciation.copy(),
        isQuestionDisplayed,
        cardReverse
    )

    companion object {
        val Default by lazy { ExercisePreference() }
    }
}