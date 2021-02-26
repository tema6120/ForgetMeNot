package com.odnovolov.forgetmenot.presentation.screen.renamedeck

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.Deck

class RenameDeckDialogState(
    purpose: RenameDeckDialogPurpose,
    typedDeckName: String = ""
) : FlowMaker<RenameDeckDialogState>() {
    val purpose: RenameDeckDialogPurpose by flowMaker(purpose)
    var typedDeckName: String by flowMaker(typedDeckName)
}

sealed class RenameDeckDialogPurpose {
    class ToRenameExistingDeck(val deck: Deck) : RenameDeckDialogPurpose()
    object ToRenameNewDeckForFileImport : RenameDeckDialogPurpose()
    object ToCreateNewDeck : RenameDeckDialogPurpose()
    object ToCreateNewForDeckChooser : RenameDeckDialogPurpose()
}