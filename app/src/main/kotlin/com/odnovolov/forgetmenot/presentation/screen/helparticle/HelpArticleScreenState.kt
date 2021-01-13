package com.odnovolov.forgetmenot.presentation.screen.helparticle

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry

class HelpArticleScreenState(
    currentArticle: HelpArticle
) : FlowMakerWithRegistry<HelpArticleScreenState>() {
    var currentArticle: HelpArticle by flowMaker(currentArticle)

    override fun copy() = HelpArticleScreenState(currentArticle)
}