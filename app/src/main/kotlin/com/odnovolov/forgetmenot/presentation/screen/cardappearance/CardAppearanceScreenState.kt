package com.odnovolov.forgetmenot.presentation.screen.cardappearance

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.Card

class CardAppearanceScreenState(
    exampleCards: List<Card>,
    textSizeDialogText: String = "",
    textSizeDialogDestination: TextSizeDialogDestination? = null,
    textOpacityInDialog: Float = -1f,
    textOpacityDialogDestination: TextOpacityDialogDestination? = null
) : FlowMaker<CardAppearanceScreenState>() {
    val exampleCards: List<Card> by flowMaker(exampleCards)
    var textSizeDialogText: String by flowMaker(textSizeDialogText)
    var textSizeDialogDestination: TextSizeDialogDestination? by flowMaker(textSizeDialogDestination)
    var textOpacityInDialog: Float by flowMaker(textOpacityInDialog)
    var textOpacityDialogDestination: TextOpacityDialogDestination? by flowMaker(textOpacityDialogDestination)

    enum class TextSizeDialogDestination {
        ForQuestion,
        ForAnswer
    }

    enum class TextOpacityDialogDestination {
        ForLightTheme,
        ForDarkTheme
    }
}