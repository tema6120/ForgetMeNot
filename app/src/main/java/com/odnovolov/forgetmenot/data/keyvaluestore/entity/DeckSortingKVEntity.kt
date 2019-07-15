package com.odnovolov.forgetmenot.data.keyvaluestore.entity

import com.odnovolov.forgetmenot.domain.entity.DeckSorting

enum class DeckSortingKVEntity(val id: Int) {

    BY_NAME(0),
    BY_TIME_CREATED(1),
    BY_LAST_OPENED(2);

    fun toDeckSorting(): DeckSorting {
        return when (this) {
            BY_NAME -> DeckSorting.BY_NAME
            BY_TIME_CREATED -> DeckSorting.BY_TIME_CREATED
            BY_LAST_OPENED -> DeckSorting.BY_LAST_OPENED
        }
    }

    companion object {
        fun fromDeckSorting(deckSorting: DeckSorting): DeckSortingKVEntity {
            return when (deckSorting) {
                DeckSorting.BY_NAME -> BY_NAME
                DeckSorting.BY_TIME_CREATED -> BY_TIME_CREATED
                DeckSorting.BY_LAST_OPENED -> BY_LAST_OPENED
            }
        }

        fun getById(id: Int): DeckSortingKVEntity? {
            return values().find { it.id == id }
        }
    }
}