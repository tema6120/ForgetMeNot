package com.odnovolov.forgetmenot.domain.interactor.fileimport

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.AbstractDeck
import com.odnovolov.forgetmenot.domain.interactor.fileimport.Parser.Error
import java.nio.charset.Charset

class CardsFile(
    id: Long,
    sourceBytes: ByteArray,
    charset: Charset,
    text: String,
    format: FileFormat,
    errors: List<Error>,
    cardPrototypes: List<CardPrototype>,
    deckWhereToAdd: AbstractDeck,
) : FlowMaker<CardsFile>() {
    val id: Long by flowMaker(id)
    val sourceBytes: ByteArray by flowMaker(sourceBytes)
    var charset: Charset by flowMaker(charset)
    var text: String by flowMaker(text)
    var format: FileFormat by flowMaker(format)
    var errors: List<Error> by flowMaker(errors)
    var cardPrototypes: List<CardPrototype> by flowMaker(cardPrototypes)
    var deckWhereToAdd: AbstractDeck by flowMaker(deckWhereToAdd)
}