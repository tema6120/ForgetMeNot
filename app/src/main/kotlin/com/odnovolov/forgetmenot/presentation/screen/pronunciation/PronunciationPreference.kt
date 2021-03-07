package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import java.util.*

class PronunciationPreference(
    favoriteLanguages: Set<Locale>
) : FlowMakerWithRegistry<PronunciationPreference>() {
    var favoriteLanguages: Set<Locale> by flowMaker(
        favoriteLanguages,
        preferredChangeClass = PropertyValueChange::class
    )

    override fun copy() = PronunciationPreference(favoriteLanguages)
}