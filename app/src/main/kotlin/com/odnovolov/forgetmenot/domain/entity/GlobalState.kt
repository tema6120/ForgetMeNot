package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableCollection
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.CollectionChange
import com.odnovolov.forgetmenot.domain.architecturecomponents.RegistrableFlowableState

class GlobalState(
    decks: CopyableCollection<Deck>,
    sharedExercisePreferences: CopyableCollection<ExercisePreference>,
    sharedIntervalSchemes: CopyableCollection<IntervalScheme>,
    sharedPronunciations: CopyableCollection<Pronunciation>,
    sharedSpeakPlans: CopyableCollection<SpeakPlan>,
    sharedRepetitionSettings: CopyableCollection<RepetitionSetting>,
    currentRepetitionSetting: RepetitionSetting,
    isWalkingModeEnabled: Boolean
) : RegistrableFlowableState<GlobalState>() {
    var decks: CopyableCollection<Deck> by me(decks, CollectionChange::class)

    var sharedExercisePreferences: CopyableCollection<ExercisePreference>
            by me(sharedExercisePreferences, CollectionChange::class)

    var sharedIntervalSchemes: CopyableCollection<IntervalScheme>
            by me(sharedIntervalSchemes, CollectionChange::class)

    var sharedPronunciations: CopyableCollection<Pronunciation>
            by me(sharedPronunciations, CollectionChange::class)

    var sharedSpeakPlans: CopyableCollection<SpeakPlan>
            by me(sharedSpeakPlans, CollectionChange::class)

    var sharedRepetitionSettings: CopyableCollection<RepetitionSetting>
            by me(sharedRepetitionSettings, CollectionChange::class)

    var currentRepetitionSetting: RepetitionSetting by me(currentRepetitionSetting)

    var isWalkingModeEnabled: Boolean by me(isWalkingModeEnabled)

    override fun copy() = GlobalState(
        decks.copy(),
        sharedExercisePreferences.copy(),
        sharedIntervalSchemes.copy(),
        sharedPronunciations.copy(),
        sharedSpeakPlans.copy(),
        sharedRepetitionSettings.copy(),
        currentRepetitionSetting.copy(),
        isWalkingModeEnabled
    )
}