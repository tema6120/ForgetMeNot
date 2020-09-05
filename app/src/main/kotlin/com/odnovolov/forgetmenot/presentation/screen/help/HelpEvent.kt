package com.odnovolov.forgetmenot.presentation.screen.help

sealed class HelpEvent {
    class ArticleClickedInTableOfContents(val helpArticle: HelpArticle) : HelpEvent()
    class ArticleLinkClicked(val helpArticle: HelpArticle) : HelpEvent()
    class ArticleOpened(val helpArticle: HelpArticle) : HelpEvent()
}