package com.odnovolov.forgetmenot.domain.interactor.fileimport

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker

abstract class Parser(
    val state: State
) {
    abstract fun parse(text: String)

    class State(
        text: String,
        cardPrototypes: List<CardPrototype>,
        errorLines: List<Int>
    ) : FlowMaker<State>() {
        var text: String by flowMaker(text)
        var cardPrototypes: List<CardPrototype> by flowMaker(cardPrototypes)
        var errorLines: List<Int> by flowMaker(errorLines)
    }

    data class CardPrototype(
        val questionRange: IntRange,
        val answerRange: IntRange
    )
}