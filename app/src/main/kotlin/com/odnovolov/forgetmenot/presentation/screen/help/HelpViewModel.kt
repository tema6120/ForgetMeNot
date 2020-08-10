package com.odnovolov.forgetmenot.presentation.screen.help

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HelpViewModel(
    helpScreenState: HelpScreenState
) {
    val currentHelpArticle: Flow<HelpArticle> = helpScreenState
        .flowOf(HelpScreenState::currentArticle)

    val helpArticleItems: Flow<List<HelpArticleItem>> = currentHelpArticle
        .map { currentHelpArticle: HelpArticle ->
            HelpArticle.values()
                .map { helpArticle: HelpArticle ->
                    HelpArticleItem(
                        helpArticle,
                        isArticleSelected = helpArticle == currentHelpArticle
                    )
                }
        }
}