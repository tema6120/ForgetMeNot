package com.odnovolov.forgetmenot.presentation.screen.home

data class SelectableDeckList(
    val id: Long?, // null if all decks
    val name: String?, // null if all decks
    val color: Int,
    val size: Int,
    val isSelected: Boolean
)