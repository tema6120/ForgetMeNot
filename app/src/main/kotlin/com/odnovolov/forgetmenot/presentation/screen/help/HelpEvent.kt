package com.odnovolov.forgetmenot.presentation.screen.help

sealed class HelpEvent {
    class ArticleSelected(val helpArticle: HelpArticle) : HelpEvent()
    class ArticleLinkClicked(val helpArticle: HelpArticle) : HelpEvent()
    class ArticleOpened(val helpArticle: HelpArticle) : HelpEvent()
}