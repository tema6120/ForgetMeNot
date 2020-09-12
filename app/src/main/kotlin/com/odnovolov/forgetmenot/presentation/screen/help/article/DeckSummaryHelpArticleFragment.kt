package com.odnovolov.forgetmenot.presentation.screen.help.article

import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.presentation.common.setTextWithClickableAnnotations
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle.Exercise
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle.Repetition
import com.odnovolov.forgetmenot.presentation.screen.help.HelpEvent.ArticleLinkClicked
import kotlinx.android.synthetic.main.article_deck_summary.*

class DeckSummaryHelpArticleFragment : BaseHelpArticleFragmentForComplexUi() {
    private class State : FlowMaker<State>() {
        var isSelected: Boolean by flowMaker(false)
    }

    private val state = State()
    override val layoutRes: Int get() = R.layout.article_deck_summary
    override val helpArticle: HelpArticle get() = HelpArticle.DeckSummary

    override fun setupView() {
        deckButton.setOnClickListener {
            if (state.isSelected) state.isSelected = false
        }
        deckButton.setOnLongClickListener {
            state.isSelected = !state.isSelected
            true
        }
        state.flowOf(State::isSelected).observe(deckButton::setSelected)
        paragraph1.setTextWithClickableAnnotations(
            stringId = R.string.article_deck_summary_paragraph_1,
            onAnnotationClick = { annotationValue: String ->
                when (annotationValue) {
                    "level_of_knowledge" -> controller?.dispatch(
                        ArticleLinkClicked(HelpArticle.LevelOfKnowledgeAndIntervals)
                    )
                }
            }
        )
        paragraph2.setTextWithClickableAnnotations(
            stringId = R.string.article_deck_summary_paragraph_2,
            onAnnotationClick = { annotationValue: String ->
                when (annotationValue) {
                    "exercise" -> controller?.dispatch(ArticleLinkClicked(Exercise))
                    "repetition" -> controller?.dispatch(ArticleLinkClicked(Repetition))
                }
            }
        )
    }
}