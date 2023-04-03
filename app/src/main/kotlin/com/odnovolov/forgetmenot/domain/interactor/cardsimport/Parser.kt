package com.odnovolov.forgetmenot.domain.interactor.cardsimport

abstract class Parser {
    abstract fun parse(text: String): ParserResult

    data class ParserResult(
        val cardMarkups: List<CardMarkup>,
        val errors: List<Error>
    )

    data class CardMarkup(
        val questionText: String,
        val questionRange: IntRange?,
        val answerText: String,
        val answerRange: IntRange?
    )

    data class Error(
        val message: String,
        val errorRange: IntRange
    )
}