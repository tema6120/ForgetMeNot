package com.odnovolov.forgetmenot.presentation.screen.helparticle

sealed class HelpArticleEvent {
    class ArticleSelected(val helpArticle: HelpArticle) : HelpArticleEvent()
    class ArticleLinkClicked(val helpArticle: HelpArticle) : HelpArticleEvent()
    class ArticleOpened(val helpArticle: HelpArticle) : HelpArticleEvent()
    object PreviousArticleButtonClicked : HelpArticleEvent()
    object NextArticleButtonClicked : HelpArticleEvent()
}