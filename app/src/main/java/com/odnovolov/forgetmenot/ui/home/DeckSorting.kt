package com.odnovolov.forgetmenot.ui.home

enum class DeckSorting(val id: Int) {

    BY_TIME_CREATED(0),
    BY_NAME(1),
    BY_LAST_OPENED(2);

    companion object {
        fun getById(id: Int): DeckSorting? {
            return values().find { it.id == id }
        }
    }
}