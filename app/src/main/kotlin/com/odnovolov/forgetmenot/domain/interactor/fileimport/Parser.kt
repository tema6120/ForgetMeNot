package com.odnovolov.forgetmenot.domain.interactor.fileimport

abstract class Parser {
    abstract fun parse(text: String): ParserResult

    data class ParserResult(
        val cardMarkups: List<CardMarkup>,
        val errorRanges: List<IntRange>
    )

    data class CardMarkup(
        val questionText: String,
        val questionRange: IntRange?,
        val answerText: String,
        val answerRange: IntRange?
    )
}