package com.odnovolov.forgetmenot.domain.interactor.deckcreator

import com.odnovolov.forgetmenot.domain.checkDeckName
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.InvalidNameException
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.Ok
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor.State.Mode.Creation

class DeckCreator(
    private val globalState: GlobalState
) {
    fun create(deckName: String): CardsEditor.State {
        return when (val nameCheckResult = checkDeckName(deckName, globalState)) {
            Ok -> CardsEditor.State(mode = Creation(deckName))
            else -> throw InvalidNameException(nameCheckResult)
        }
    }
}