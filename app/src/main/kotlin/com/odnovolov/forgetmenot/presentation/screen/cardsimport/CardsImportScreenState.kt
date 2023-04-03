package com.odnovolov.forgetmenot.presentation.screen.cardsimport

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker

class CardsImportScreenState(
    wasAskedToUseSelectedDeckForImportNextFiles: Boolean = false
) : FlowMaker<CardsImportScreenState>() {
    var wasAskedToUseSelectedDeckForImportNextFiles: Boolean
            by flowMaker(wasAskedToUseSelectedDeckForImportNextFiles)
}