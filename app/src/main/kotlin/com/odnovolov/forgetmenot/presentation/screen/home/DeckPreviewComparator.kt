package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Criterion.*
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Asc
import com.odnovolov.forgetmenot.presentation.screen.home.HomeViewModel.RawDeckPreview

class DeckPreviewComparator(
    private val deckSorting: DeckSorting
) : Comparator<RawDeckPreview> {
    override fun compare(deck1: RawDeckPreview, deck2: RawDeckPreview): Int {
        return when {
            deck1.lastTestedAt == null && deck2.lastTestedAt == null -> {
                deck2.createdAt.compareTo(deck1.createdAt)
            }
            deck1.lastTestedAt == null -> {
                -1
            }
            deck2.lastTestedAt == null -> {
                1
            }
            else -> {
                val leftDeck = if (deckSorting.direction == Asc) deck1 else deck2
                val rightDeck = if (deckSorting.direction == Asc) deck2 else deck1
                when (deckSorting.criterion) {
                    Name -> leftDeck.deckName.compareTo(rightDeck.deckName)
                    CreatedAt -> leftDeck.createdAt.compareTo(rightDeck.createdAt)
                    LastOpenedAt -> leftDeck.lastTestedAt!!.compareTo(rightDeck.lastTestedAt!!)
                }
            }
        }
    }
}