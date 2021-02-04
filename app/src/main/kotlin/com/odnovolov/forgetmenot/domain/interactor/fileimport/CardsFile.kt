package com.odnovolov.forgetmenot.domain.interactor.fileimport

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.AbstractDeck
import java.nio.charset.Charset

class CardsFile(
    deckWhereToAdd: AbstractDeck,
    text: String,
    charset: Charset
) : FlowMaker<CardsFile>() {
    var deckWhereToAdd: AbstractDeck by flowMaker(deckWhereToAdd)
    var text: String by flowMaker(text)
    var charset: Charset by flowMaker(charset)
}