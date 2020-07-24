package com.odnovolov.forgetmenot.domain.interactor.deckexporter

import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import java.io.OutputStream

class DeckExporter {
    fun export(deck: Deck, outputStream: OutputStream) {
        val stringToWrite = deck.cards.joinToString(separator = "\n\n\n\n") { card: Card ->
            "Q:\n${card.question}\nA:\n${card.answer}"
        }
        outputStream.use { os ->
            os.bufferedWriter().use { bufferedWriter ->
                bufferedWriter.write(stringToWrite)
            }
        }
    }
}