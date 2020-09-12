package com.odnovolov.forgetmenot.presentation.screen.help

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry

class HelpScreenState(
    currentArticle: HelpArticle
) : FlowMakerWithRegistry<HelpScreenState>() {
    var currentArticle: HelpArticle by flowMaker(currentArticle)

    override fun copy() = HelpScreenState(currentArticle)
}