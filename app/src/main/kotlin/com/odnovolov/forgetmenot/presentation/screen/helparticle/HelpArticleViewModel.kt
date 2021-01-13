package com.odnovolov.forgetmenot.presentation.screen.helparticle

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HelpArticleViewModel(
    helpScreenState: HelpArticleScreenState
) {
    val currentArticle: Flow<HelpArticle> =
        helpScreenState.flowOf(HelpArticleScreenState::currentArticle)

    val isPreviousArticleButtonEnabled: Flow<Boolean> = currentArticle
        .map { currentHelpArticle: HelpArticle ->
            HelpArticle.values().indexOf(currentHelpArticle) > 0
        }

    val isNextArticleButtonEnabled: Flow<Boolean> = currentArticle
        .map { currentHelpArticle: HelpArticle ->
            HelpArticle.values().run {
                indexOf(currentHelpArticle) < lastIndex
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