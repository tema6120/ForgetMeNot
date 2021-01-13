package com.odnovolov.forgetmenot.presentation.screen.help

import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle

sealed class HelpEvent {
    class ArticleItemClicked(val helpArticle: HelpArticle) : HelpEvent()
}