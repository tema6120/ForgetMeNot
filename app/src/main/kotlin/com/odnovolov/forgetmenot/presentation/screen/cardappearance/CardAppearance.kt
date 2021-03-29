package com.odnovolov.forgetmenot.presentation.screen.cardappearance

import android.view.Gravity
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry

class CardAppearance(
    questionTextAlignment: CardTextAlignment,
    questionTextSize: Int,
    answerTextAlignment: CardTextAlignment,
    answerTextSize: Int,
    textOpacityInLightTheme: Float,
    textOpacityInDarkTheme: Float
) : FlowMakerWithRegistry<CardAppearance>() {
    var questionTextAlignment: CardTextAlignment by flowMaker(questionTextAlignment)
    var questionTextSize: Int by flowMaker(questionTextSize)
    var answerTextAlignment: CardTextAlignment by flowMaker(answerTextAlignment)
    var answerTextSize: Int by flowMaker(answerTextSize)
    var textOpacityInLightTheme: Float by flowMaker(textOpacityInLightTheme)
    var textOpacityInDarkTheme: Float by flowMaker(textOpacityInDarkTheme)

    override fun copy() = CardAppearance(
        questionTextAlignment,
        questionTextSize,
        answerTextAlignment,
        answerTextSize,
        textOpacityInLightTheme,
        textOpacityInDarkTheme
    )

    companion object {
        val DEFAULT_QUESTION_TEXT_ALIGNMENT = CardTextAlignment.Center
        const val DEFAULT_QUESTION_TEXT_SIZE = 19
        val DEFAULT_ANSWER_TEXT_ALIGNMENT = CardTextAlignment.Edge
        const val DEFAULT_ANSWER_TEXT_SIZE = 17
        const val DEFAULT_TEXT_OPACITY_IN_LIGHT_THEME = 0.60F
        const val DEFAULT_TEXT_OPACITY_IN_DARK_THEME = 0.67F
    }
}

enum class CardTextAlignment(val gravity: Int) {
    Edge(Gravity.TOP or Gravity.START),
    Center(Gravity.CENTER)
}