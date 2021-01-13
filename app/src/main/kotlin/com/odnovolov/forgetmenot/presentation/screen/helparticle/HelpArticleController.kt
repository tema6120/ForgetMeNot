package com.odnovolov.forgetmenot.presentation.screen.helparticle

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleController.Command
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleController.Command.OpenArticle
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleEvent.*

class HelpArticleController(
    private val screenState: HelpArticleScreenState,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<HelpArticleEvent, Command>() {
    sealed class Command {
        class OpenArticle(val article: HelpArticle, val needToClearBackStack: Boolean) : Command()
    }

    override fun handle(event: HelpArticleEvent) {
        when (event) {
            is ArticleSelected -> {
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

            PreviousArticleButtonClicked -> {
                val articles = HelpArticle.values()
                val currentIndex = articles.indexOf(screenState.currentArticle)
                if (currentIndex > 0) {
                    val article = articles[currentIndex - 1]
                    sendCommand(OpenArticle(article, needToClearBackStack = true))
                }
            }

            NextArticleButtonClicked -> {
                val articles = HelpArticle.values()
                val currentIndex = articles.indexOf(screenState.currentArticle)
                if (currentIndex < articles.lastIndex) {
                    val article = articles[currentIndex + 1]
                    sendCommand(OpenArticle(article, needToClearBackStack = true))
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}