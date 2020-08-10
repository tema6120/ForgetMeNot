package com.odnovolov.forgetmenot.presentation.screen.help

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.help.HelpEvent.BackButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.help.HelpEvent.HelpArticleSelected

class HelpController(
    private val screenState: HelpScreenState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<HelpEvent, Nothing>() {
    override fun handle(event: HelpEvent) {
        when (event) {
            BackButtonClicked -> {
                navigator.navigateUp()
            }

            is HelpArticleSelected -> {
                screenState.currentArticle = event.helpArticle
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}