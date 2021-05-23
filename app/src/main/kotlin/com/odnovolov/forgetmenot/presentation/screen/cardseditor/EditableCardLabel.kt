package com.odnovolov.forgetmenot.presentation.screen.cardseditor

import com.odnovolov.forgetmenot.R

enum class EditableCardLabel(
    val textResId: Int,
    val colorResId: Int
) {
    DUPLICATED(
        R.string.card_label_duplicated,
        R.color.card_label_duplicated
    ),
    CURRENT_IN_EXERCISE(
        R.string.card_label_current_in_exercise,
        R.color.card_label_current_in_exercise
    ),
    CURRENT_IN_PLAYER(
        R.string.card_label_current_in_player,
        R.color.card_label_current_in_exercise
    ),
    FOUND(
        R.string.card_label_found,
        R.color.card_label_found
    ),
    NEW(
        R.string.card_label_new,
        R.color.card_label_new
    )
}