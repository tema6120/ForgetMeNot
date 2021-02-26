package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.soywiz.klock.DateTime

class Deck(
    override val id: Long,
    name: String,
    createdAt: DateTime = DateTime.now(),
    lastTestedAt: DateTime? = null,
    cards: CopyableList<Card>,
    exercisePreference: ExercisePreference = ExercisePreference.Default,
    isPinned: Boolean = false
) : FlowMakerWithRegistry<Deck>() {
    var name: String by flowMaker(name)
    val createdAt: DateTime by flowMaker(createdAt)
    var lastTestedAt: DateTime? by flowMaker(lastTestedAt)
    var cards: CopyableList<Card> by flowMaker(cards)
    var exercisePreference: ExercisePreference by flowMaker(exercisePreference)
    var isPinned: Boolean by flowMaker(isPinned)

    override fun copy() = Deck(
        id,
        name,
        createdAt,
        lastTestedAt,
        cards.copy(),
        exercisePreference.copy(),
        isPinned
    )
}