package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableCollection
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.CollectionChange
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry

class GlobalState(
    decks: CopyableCollection<Deck>,
    sharedExercisePreferences: CopyableCollection<ExercisePreference>,
    cardFilterForAutoplay: CardFilterForAutoplay,
    isWalkingModeEnabled: Boolean,
    isInfinitePlaybackEnabled: Boolean
) : FlowMakerWithRegistry<GlobalState>() {
    var decks: CopyableCollection<Deck> by flowMaker(decks, CollectionChange::class)

    var sharedExercisePreferences: CopyableCollection<ExercisePreference>
            by flowMaker(sharedExercisePreferences, CollectionChange::class)

    val cardFilterForAutoplay: CardFilterForAutoplay by flowMaker(cardFilterForAutoplay)

    var isWalkingModeEnabled: Boolean by flowMaker(isWalkingModeEnabled)

    var isInfinitePlaybackEnabled: Boolean by flowMaker(isInfinitePlaybackEnabled)

    override fun copy() = GlobalState(
        decks.copy(),
        sharedExercisePreferences.copy(),
        cardFilterForAutoplay.copy(),
        isWalkingModeEnabled,
        isInfinitePlaybackEnabled
    )

    companion object {
        const val DEFAULT_IS_WALKING_MODE_ENABLED = false
        const val DEFAULT_IS_INFINITE_PLAYBACK_ENABLED = false
    }
}