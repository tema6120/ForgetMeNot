package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableCollection
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.CollectionChange
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry

class GlobalState(
    decks: CopyableCollection<Deck>,
    sharedExercisePreferences: CopyableCollection<ExercisePreference>,
    sharedPronunciationPlans: CopyableCollection<PronunciationPlan>,
    cardFilterForAutoplay: CardFilterForAutoplay,
    isWalkingModeEnabled: Boolean,
    isInfinitePlaybackEnabled: Boolean
) : FlowMakerWithRegistry<GlobalState>() {
    var decks: CopyableCollection<Deck> by flowMaker(decks, CollectionChange::class)

    var sharedExercisePreferences: CopyableCollection<ExercisePreference>
            by flowMaker(sharedExercisePreferences, CollectionChange::class)

    var sharedPronunciationPlans: CopyableCollection<PronunciationPlan>
            by flowMaker(sharedPronunciationPlans, CollectionChange::class)

    val cardFilterForAutoplay: CardFilterForAutoplay by flowMaker(cardFilterForAutoplay)

    var isWalkingModeEnabled: Boolean by flowMaker(isWalkingModeEnabled)

    var isInfinitePlaybackEnabled: Boolean by flowMaker(isInfinitePlaybackEnabled)

    override fun copy() = GlobalState(
        decks.copy(),
        sharedExercisePreferences.copy(),
        sharedPronunciationPlans.copy(),
        cardFilterForAutoplay.copy(),
        isWalkingModeEnabled,
        isInfinitePlaybackEnabled
    )
}