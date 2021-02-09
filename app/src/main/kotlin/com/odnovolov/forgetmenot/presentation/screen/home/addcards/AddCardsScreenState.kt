package com.odnovolov.forgetmenot.presentation.screen.home.addcards

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker

class AddCardsScreenState : FlowMaker<AddCardsScreenState>() {
    var typedText: String by flowMaker("")
    var isDeckBeingCreated: Boolean by flowMaker(false)
    var areFilesBeingReading: Boolean by flowMaker(false)
}