package com.odnovolov.forgetmenot.presentation.screen.helparticle

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker

class HelpArticleScreenState(
    currentArticle: HelpArticle
) : FlowMaker<HelpArticleScreenState>() {
    var currentArticle: HelpArticle by flowMaker(currentArticle)
}