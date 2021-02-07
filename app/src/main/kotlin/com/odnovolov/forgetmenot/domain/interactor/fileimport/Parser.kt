package com.odnovolov.forgetmenot.domain.interactor.fileimport

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker

abstract class Parser(
    state: State
) : FlowMaker<Parser>() {
    var state: State by flowMaker(state)

    abstract fun parse(text: String)

    data class State(
        val text: String,
        val cardPrototypes: List<CardPrototype>,
        val errorLines: List<Int>
    )

    data class CardPrototype(
        val questionRange: IntRange,
        val answerRange: IntRange
    )
}