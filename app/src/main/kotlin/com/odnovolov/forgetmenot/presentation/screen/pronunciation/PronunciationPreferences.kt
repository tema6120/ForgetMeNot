package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import java.util.*

class PronunciationPreferences(
    favoriteLanguages: Set<Locale>
) : FlowMakerWithRegistry<PronunciationPreferences>() {
    var favoriteLanguages: Set<Locale> by flowMaker(favoriteLanguages)

    override fun copy() = PronunciationPreferences(favoriteLanguages)
}