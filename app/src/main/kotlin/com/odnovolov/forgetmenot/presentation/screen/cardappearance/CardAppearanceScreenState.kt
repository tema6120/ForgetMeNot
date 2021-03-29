package com.odnovolov.forgetmenot.presentation.screen.cardappearance

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.Card

class CardAppearanceScreenState(
    exampleCards: List<Card>,
    textSizeDialogText: String = "",
    textSizeDialogDestination: TextSizeDialogDestination? = null
) : FlowMaker<CardAppearanceScreenState>() {
    val exampleCards: List<Card> by flowMaker(exampleCards)
    var textSizeDialogText: String by flowMaker(textSizeDialogText)
    var textSizeDialogDestination: TextSizeDialogDestination? by flowMaker(textSizeDialogDestination)

    enum class TextSizeDialogDestination {
        ForQuestion,
        ForAnswer
    }
}