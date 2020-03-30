package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.CopyableList
import com.odnovolov.forgetmenot.domain.architecturecomponents.RegistrableFlowableState

class GlobalState(
    decks: CopyableList<Deck>,
    sharedExercisePreferences: CopyableList<ExercisePreference>,
    sharedIntervalSchemes: CopyableList<IntervalScheme>,
    sharedPronunciations: CopyableList<Pronunciation>,
    savedRepetitionSettings: CopyableList<RepetitionSetting>,
    currentRepetitionSetting: RepetitionSetting
) : RegistrableFlowableState<GlobalState>() {
    var decks: CopyableList<Deck> by me(decks)
    var sharedExercisePreferences: CopyableList<ExercisePreference> by me(sharedExercisePreferences)
    var sharedIntervalSchemes: CopyableList<IntervalScheme> by me(sharedIntervalSchemes)
    var sharedPronunciations: CopyableList<Pronunciation> by me(sharedPronunciations)
    var savedRepetitionSettings: CopyableList<RepetitionSetting> by me(savedRepetitionSettings)
    var currentRepetitionSetting: RepetitionSetting by me(currentRepetitionSetting)

    override fun copy() = GlobalState(
        decks.copy(),
        sharedExercisePreferences.copy(),
        sharedIntervalSchemes.copy(),
        sharedPronunciations.copy(),
        savedRepetitionSettings.copy(),
        currentRepetitionSetting.copy()
    )
}