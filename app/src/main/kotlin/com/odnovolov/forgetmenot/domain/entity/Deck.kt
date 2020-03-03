package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.architecturecomponents.RegistrableFlowableState
import com.soywiz.klock.DateTime

class Deck(
    override val id: Long,
    name: String,
    createdAt: DateTime = DateTime.now(),
    lastOpenedAt: DateTime? = null,
    cards: CopyableList<Card>,
    exercisePreference: ExercisePreference = ExercisePreference.Default
) : RegistrableFlowableState<Deck>() {
    var name: String by me(name)
    val createdAt: DateTime by me(createdAt)
    var lastOpenedAt: DateTime? by me(lastOpenedAt)
    var cards: CopyableList<Card> by me(cards)
    var exercisePreference: ExercisePreference by me(exercisePreference)

    override fun copy() = Deck(
        id,
        name,
        createdAt,
        lastOpenedAt,
        cards.copy(),
        exercisePreference.copy()
    )
}