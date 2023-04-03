package com.odnovolov.forgetmenot.domain.interactor.cardsimport

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.AbstractDeck
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.Parser.Error
import java.nio.charset.Charset

class CardsFile(
    id: Long,
    extension: String,
    sourceBytes: ByteArray,
    charset: Charset,
    text: String,
    format: CardsFileFormat,
    errors: List<Error>,
    cardPrototypes: List<CardPrototype>,
    deckWhereToAdd: AbstractDeck,
) : FlowMaker<CardsFile>() {
    val id: Long by flowMaker(id)
    val extension: String by flowMaker(extension)
    val sourceBytes: ByteArray by flowMaker(sourceBytes)
    var charset: Charset by flowMaker(charset)
    var text: String by flowMaker(text)
    var format: CardsFileFormat by flowMaker(format)
    var errors: List<Error> by flowMaker(errors)
    var cardPrototypes: List<CardPrototype> by flowMaker(cardPrototypes)
    var deckWhereToAdd: AbstractDeck by flowMaker(deckWhereToAdd)
}