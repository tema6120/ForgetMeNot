package com.odnovolov.forgetmenot.presentation.screen.exercise

data class CardPosition(
    val position: Int,
    val total: Int
) {
    override fun toString(): String {
        return "$position/$total"
    }
}