package com.odnovolov.forgetmenot.domain.interactor.fileimport

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.AbstractDeck
import java.nio.charset.Charset

class CardsFile(
    sourceBytes: ByteArray,
    charset: Charset,
    deckWhereToAdd: AbstractDeck,
    text: String
) : FlowMaker<CardsFile>() {
    val sourceBytes: ByteArray by flowMaker(sourceBytes)
    var charset: Charset by flowMaker(charset)
    var deckWhereToAdd: AbstractDeck by flowMaker(deckWhereToAdd)
    var text: String by flowMaker(text)
}