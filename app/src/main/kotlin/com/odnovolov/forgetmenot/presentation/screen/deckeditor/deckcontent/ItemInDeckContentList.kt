package com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent

import com.odnovolov.forgetmenot.domain.entity.Card

sealed class ItemInDeckContentList {
    object Header : ItemInDeckContentList()

    data class SelectableCard(
        val card: Card,
        val isSelected: Boolean
    ) : ItemInDeckContentList()
}