package com.odnovolov.forgetmenot.presentation.screen.player.view.playingcard

sealed class CardContent {
    data class UnansweredCard(val question: String) : CardContent()
    data class AnsweredCard(val question: String, val answer: String) : CardContent()
}