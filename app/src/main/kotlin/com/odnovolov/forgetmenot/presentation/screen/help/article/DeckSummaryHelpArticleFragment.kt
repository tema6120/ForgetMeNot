package com.odnovolov.forgetmenot.presentation.screen.help.article

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.setTextWithClickableAnnotations
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle.*
import com.odnovolov.forgetmenot.presentation.screen.help.HelpController
import com.odnovolov.forgetmenot.presentation.screen.help.HelpDiScope
import com.odnovolov.forgetmenot.presentation.screen.help.HelpEvent.ArticleLinkClicked
import com.odnovolov.forgetmenot.presentation.screen.help.HelpEvent.ArticleOpened
import kotlinx.android.synthetic.main.article_deck_summary.*
import kotlinx.android.synthetic.main.article_deck_summary.view.*
import kotlinx.coroutines.launch

class DeckSummaryHelpArticleFragment : BaseFragment() {
    init {
        HelpDiScope.reopenIfClosed()
    }

    private class State : FlowableState<State>() {
        var isSelected: Boolean by me(false)
    }

    private val state = State()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.article_deck_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        deckButton.setOnClickListener {
            if (state.isSelected) state.isSelected = false
        }
        deckButton.setOnLongClickListener {
            state.isSelected = !state.isSelected
            true
        }
        state.flowOf(State::isSelected).observe(deckButton::setSelected)
        viewCoroutineScope!!.launch {
            val diScope = HelpDiScope.getAsync() ?: return@launch
            val controller: HelpController = diScope.controller
            view.paragraph1.setTextWithClickableAnnotations(
                stringId = R.string.article_deck_summary_paragraph_1,
                onAnnotationClick = { annotationValue: String ->
                    when (annotationValue) {
                        "level_of_knowledge" -> controller.dispatch(
                            ArticleLinkClicked(LevelOfKnowledgeAndIntervals)
                        )
                    }
                }
            )
            view.paragraph2.setTextWithClickableAnnotations(
                stringId = R.string.article_deck_summary_paragraph_2,
                onAnnotationClick = { annotationValue: String ->
                    when (annotationValue) {
                        "exercise" -> controller.dispatch(ArticleLinkClicked(Exercise))
                        "repetition" -> controller.dispatch(ArticleLinkClicked(Repetition))
                    }
                }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        viewCoroutineScope!!.launch {
            val diScope = HelpDiScope.getAsync() ?: return@launch
            diScope.controller.dispatch(ArticleOpened(DeckSummary))
        }
    }
}