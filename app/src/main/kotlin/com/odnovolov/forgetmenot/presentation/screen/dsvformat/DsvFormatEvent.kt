package com.odnovolov.forgetmenot.presentation.screen.dsvformat

sealed class DsvFormatEvent {
    class IgnoreSurroundingSpacesButton(val ignoreSurroundingSpaces: Boolean) : DsvFormatEvent()
}