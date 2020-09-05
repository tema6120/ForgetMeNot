package com.odnovolov.forgetmenot.presentation.screen.help

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.help.HelpController.Command
import com.odnovolov.forgetmenot.presentation.screen.help.HelpController.Command.OpenArticle
import com.odnovolov.forgetmenot.presentation.screen.help.HelpEvent.*

class HelpController(
    private val screenState: HelpScreenState,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<HelpEvent, Command>() {
    sealed class Command {
        class OpenArticle(val article: HelpArticle, val needToClearBackStack: Boolean) : Command()
    }

    override fun handle(event: HelpEvent) {
        when (event) {
            is ArticleClickedInTableOfContents -> {
                sendCommand(OpenArticle(event.helpArticle, needToClearBackStack = true))
            }

            is ArticleLinkClicked -> {
                sendCommand(OpenArticle(event.helpArticle, needToClearBackStack = false))
            }

            is ArticleOpened -> {
                if (screenState.currentArticle != event.helpArticle) {
                    screenState.currentArticle = event.helpArticle
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}