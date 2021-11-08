package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Asc
import com.odnovolov.forgetmenot.presentation.screen.home.HomeViewModel.RawDeckPreview

class DeckPreviewComparator(
    private val deckSorting: DeckSorting
) : Comparator<RawDeckPreview> {
    override fun compare(
        deck1: RawDeckPreview,
        deck2: RawDeckPreview
    ): Int {
        return if (deckSorting.newDecksFirst) {
            compareConsideringWhetherDeckIsNew(deck1, deck2)
        } else {
            compareConsideringWhetherDeckIsPinned(deck1, deck2)
        }
    }

    private fun compareConsideringWhetherDeckIsNew(
        deck1: RawDeckPreview,
        deck2: RawDeckPreview
    ): Int {
        return when {
            deck1.lastTestedAt == null && deck2.lastTestedAt == null ->
                deck2.createdAt.compareTo(deck1.createdAt)
            deck1.lastTestedAt == null -> -1
            deck2.lastTestedAt == null -> 1
            else -> compareConsideringWhetherDeckIsPinned(deck1, deck2)
        }
    }

    private fun compareConsideringWhetherDeckIsPinned(
        deck1: RawDeckPreview,
        deck2: RawDeckPreview
    ): Int {
        return when {
            deck1.isPinned && deck2.isPinned ->
                compareAccordingToDirectionAndCriterion(deck1, deck2)
            deck1.isPinned -> -1
            deck2.isPinned -> 1
            else -> compareAccordingToDirectionAndCriterion(deck1, deck2)
        }
    }

    private fun compareAccordingToDirectionAndCriterion(
        deck1: RawDeckPreview,
        deck2: RawDeckPreview
    ): Int {
        val leftDeck: RawDeckPreview = if (deckSorting.direction == Asc) deck1 else deck2
        val rightDeck: RawDeckPreview = if (deckSorting.direction == Asc) deck2 else deck1
        return compareValuesBy(leftDeck, rightDeck, deckSorting.criterion.selector)
    }
}