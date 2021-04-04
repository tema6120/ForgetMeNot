package com.odnovolov.forgetmenot.presentation.screen.helparticle

sealed class HelpArticleEvent {
    class ArticleWasSelected(val helpArticle: HelpArticle) : HelpArticleEvent()
    class ArticleLinkClicked(val helpArticle: HelpArticle) : HelpArticleEvent()
    class ArticleWasOpened(val helpArticle: HelpArticle) : HelpArticleEvent()
    object PreviousArticleButtonClicked : HelpArticleEvent()
    object NextArticleButtonClicked : HelpArticleEvent()
}