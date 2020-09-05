package com.odnovolov.forgetmenot.presentation.screen.help

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HelpViewModel(
    helpScreenState: HelpScreenState
) {
    val currentArticle: Flow<HelpArticle> = helpScreenState.flowOf(HelpScreenState::currentArticle)

    val previousArticle: Flow<HelpArticle?> = currentArticle
        .map { currentHelpArticle: HelpArticle ->
            HelpArticle.values().run {
                val currentIndex = indexOf(currentHelpArticle)
                getOrNull(currentIndex - 1)
            }
        }

    val nextArticle: Flow<HelpArticle?> = currentArticle
        .map { currentHelpArticle: HelpArticle ->
            HelpArticle.values().run {
                val currentIndex = indexOf(currentHelpArticle)
                getOrNull(currentIndex + 1)
            }
        }

    val articleItems: Flow<List<HelpArticleItem>> = currentArticle
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