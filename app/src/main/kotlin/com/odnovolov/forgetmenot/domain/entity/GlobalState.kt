package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableCollection
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry

class GlobalState(
    decks: CopyableCollection<Deck>,
    sharedExercisePreferences: CopyableCollection<ExercisePreference>,
    cardFilterForAutoplay: CardFilterForAutoplay,
    isWalkingModeEnabled: Boolean,
    numberOfLapsInPlayer: Int
) : FlowMakerWithRegistry<GlobalState>() {
    var decks: CopyableCollection<Deck> by flowMakerForCopyableCollection(decks)

    var sharedExercisePreferences: CopyableCollection<ExercisePreference>
            by flowMakerForCopyableCollection(sharedExercisePreferences)

    val cardFilterForAutoplay: CardFilterForAutoplay by flowMakerForCopyable(cardFilterForAutoplay)

    var isWalkingModeEnabled: Boolean by flowMaker(isWalkingModeEnabled)

    var numberOfLapsInPlayer: Int by flowMaker(numberOfLapsInPlayer)

    override fun copy() = GlobalState(
        decks.copy(),
        sharedExercisePreferences.copy(),
        cardFilterForAutoplay.copy(),
        isWalkingModeEnabled,
        numberOfLapsInPlayer
    )

    companion object {
        const val DEFAULT_IS_WALKING_MODE_ENABLED = false
        const val DEFAULT_NUMBER_OF_LAPS_IN_PLAYER = 1
    }
}