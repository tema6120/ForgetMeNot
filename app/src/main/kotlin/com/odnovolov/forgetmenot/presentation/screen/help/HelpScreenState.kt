package com.odnovolov.forgetmenot.presentation.screen.help

import com.odnovolov.forgetmenot.domain.architecturecomponents.RegistrableFlowableState

class HelpScreenState(
    currentArticle: HelpArticle
) : RegistrableFlowableState<HelpScreenState>() {
    var currentArticle: HelpArticle by me(currentArticle)

    override fun copy() = HelpScreenState(currentArticle)
}