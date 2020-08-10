package com.odnovolov.forgetmenot.presentation.screen.help

sealed class HelpEvent {
    object BackButtonClicked : HelpEvent()
    class HelpArticleSelected(val helpArticle: HelpArticle) : HelpEvent()
}