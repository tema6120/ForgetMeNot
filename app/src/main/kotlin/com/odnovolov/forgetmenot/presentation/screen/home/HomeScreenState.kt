package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import kotlinx.serialization.Serializable

class HomeScreenState : FlowMaker<HomeScreenState>() {
    var searchText: String by flowMaker("")
    var deckSelection: DeckSelection? by flowMaker(null)
    var deckForDeckOptionMenu: Deck? by flowMaker(null)
    var areFilesBeingReading: Boolean by flowMaker(false)
    var fileFormatForExport: FileFormat? by flowMaker(null)
    var chooseDeckListDialogPurpose: ChooseDeckListDialogPurpose? by flowMaker(null)
}

@Serializable
data class DeckSelection(
    val selectedDeckIds: List<Long>,
    val purpose: Purpose
) {
    enum class Purpose {
        General,
        ForExercise,
        ForAutoplay
    }
}

enum class ChooseDeckListDialogPurpose {
    ToAddDeckToDeckList,
    ToRemoveDeckFromDeckList
}