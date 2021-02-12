package com.odnovolov.forgetmenot.domain.interactor.fileimport

import kotlinx.serialization.Serializable

abstract class Parser {
    abstract fun parse(text: String): ParserResult

    data class ParserResult(
        val cardMarkups: List<CardMarkup>,
        val errors: List<ErrorBlock>
    )

    data class CardMarkup(
        val questionRange: IntRange,
        val answerRange: IntRange
    )

    @Serializable
    data class ErrorBlock(
        val lines: List<Int>
    )
}