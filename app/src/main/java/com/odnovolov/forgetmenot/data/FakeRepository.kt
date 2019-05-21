package com.odnovolov.forgetmenot.data

import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.repository.Repository

class FakeRepository : Repository {
    override fun insertDeck(deck: Deck): Long {
        return (0..10).random().toLong()
    }

    override fun getAllDeckNames(): List<String> {
        return listOf("Phrasal Verbs", "Irregular Verbs", "My Vocabulary 21.05.19")
    }

    override fun saveDeckIdAsLastInserted(deckId: Long) {
        print("Ok!")
    }
}