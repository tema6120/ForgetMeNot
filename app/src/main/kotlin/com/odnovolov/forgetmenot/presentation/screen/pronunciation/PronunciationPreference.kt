package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import java.util.*

class PronunciationPreference(
    favoriteLanguages: Set<Locale>
) : FlowMakerWithRegistry<PronunciationPreference>() {
    var favoriteLanguages: Set<Locale> by flowMaker(favoriteLanguages)

    override fun copy() = PronunciationPreference(favoriteLanguages)
}