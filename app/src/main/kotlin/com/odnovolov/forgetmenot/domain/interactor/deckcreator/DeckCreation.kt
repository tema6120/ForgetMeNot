package com.odnovolov.forgetmenot.domain.interactor.deckcreator

import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForDeckCreation
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.checkDeckName

fun createDeck(deckName: String, globalState: GlobalState): CardsEditor? {
    return when (checkDeckName(deckName, globalState)) {
        NameCheckResult.Ok -> CardsEditorForDeckCreation(deckName, globalState)
        else -> null
    }
}