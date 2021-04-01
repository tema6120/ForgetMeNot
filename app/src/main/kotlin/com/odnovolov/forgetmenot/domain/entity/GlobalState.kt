package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableCollection
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.odnovolov.forgetmenot.domain.interactor.autoplay.CardFilterForAutoplay
import com.odnovolov.forgetmenot.domain.interactor.exercise.CardFilterForExercise

class GlobalState(
    decks: CopyableCollection<Deck>,
    deckLists: CopyableCollection<DeckList>,
    sharedExercisePreferences: CopyableCollection<ExercisePreference>,
    cardFilterForExercise: CardFilterForExercise,
    cardFilterForAutoplay: CardFilterForAutoplay,
    isWalkingModeEnabled: Boolean,
    numberOfLapsInPlayer: Int
) : FlowMakerWithRegistry<GlobalState>() {
    var decks: CopyableCollection<Deck> by flowMakerForCopyableCollection(decks)

    var deckLists: CopyableCollection<DeckList> by flowMakerForCopyableCollection(deckLists)

    var sharedExercisePreferences: CopyableCollection<ExercisePreference>
            by flowMakerForCopyableCollection(sharedExercisePreferences)

    val cardFilterForExercise: CardFilterForExercise by flowMakerForCopyable(cardFilterForExercise)

    val cardFilterForAutoplay: CardFilterForAutoplay by flowMakerForCopyable(cardFilterForAutoplay)

    var isWalkingModeEnabled: Boolean by flowMaker(isWalkingModeEnabled)

    var numberOfLapsInPlayer: Int by flowMaker(numberOfLapsInPlayer)

    override fun copy() = GlobalState(
        decks.copy(),
        deckLists.copy(),
        sharedExercisePreferences.copy(),
        cardFilterForExercise.copy(),
        cardFilterForAutoplay.copy(),
        isWalkingModeEnabled,
        numberOfLapsInPlayer
    )

    companion object {
        const val DEFAULT_IS_WALKING_MODE_ENABLED = false
        const val DEFAULT_NUMBER_OF_LAPS_IN_PLAYER = 1
    }
}