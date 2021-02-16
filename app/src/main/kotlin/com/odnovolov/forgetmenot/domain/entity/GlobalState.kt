package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableCollection
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.CollectionChange

class GlobalState(
    decks: CopyableCollection<Deck>,
    sharedExercisePreferences: CopyableCollection<ExercisePreference>,
    cardFilterForAutoplay: CardFilterForAutoplay,
    isWalkingModeEnabled: Boolean,
    numberOfLapsInPlayer: Int
) : FlowMakerWithRegistry<GlobalState>() {
    var decks: CopyableCollection<Deck> by flowMaker(decks, CollectionChange::class)

    var sharedExercisePreferences: CopyableCollection<ExercisePreference>
            by flowMaker(sharedExercisePreferences, CollectionChange::class)

    val cardFilterForAutoplay: CardFilterForAutoplay by flowMaker(cardFilterForAutoplay)

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