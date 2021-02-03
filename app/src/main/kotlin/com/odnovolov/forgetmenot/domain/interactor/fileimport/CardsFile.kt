package com.odnovolov.forgetmenot.domain.interactor.fileimport

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import java.nio.charset.Charset

class CardsFile(
    deckWhereToAdd: DeckWhereToAdd,
    text: String,
    charset: Charset
) : FlowMaker<CardsFile>() {
    var deckWhereToAdd: DeckWhereToAdd by flowMaker(deckWhereToAdd)
    var text: String by flowMaker(text)
    var charset: Charset by flowMaker(charset)
}