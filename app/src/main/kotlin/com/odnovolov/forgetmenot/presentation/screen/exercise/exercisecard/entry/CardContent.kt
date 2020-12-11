package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry

sealed class CardContent {
    data class UnansweredCard(
        val question: String
    ) : CardContent()

    data class UnansweredCardWithHint(
        val question: String,
        val hint: String
    ) : CardContent()

    data class AnsweredCard(
        val question: String,
        val wrongAnswer: String?,
        val correctAnswer: String
    ) : CardContent()
}