package com.odnovolov.forgetmenot.domain.interactor.deckcreator

import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.InvalidNameException
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.Ok
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.checkDeckName
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditorForDeckCreation

class DeckCreator(
    private val globalState: GlobalState
) {
    fun create(deckName: String): CardsEditor {
        return when (val nameCheckResult = checkDeckName(deckName, globalState)) {
            Ok -> CardsEditorForDeckCreation(deckName, globalState)
            else -> throw InvalidNameException(nameCheckResult)
        }
    }
}