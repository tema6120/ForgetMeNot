package com.odnovolov.forgetmenot.presentation.screen.help.article

import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.setTextWithClickableAnnotations
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle.MotivationalTimer
import com.odnovolov.forgetmenot.presentation.screen.help.HelpEvent.ArticleLinkClicked
import kotlinx.android.synthetic.main.article.*

class AdviceOnStudyingHelpArticleFragment : BaseHelpArticleFragmentForSimpleUi() {
    override val layoutRes: Int get() = R.layout.article
    override val helpArticle: HelpArticle get() = HelpArticle.AdviceOnStudying

    override fun setupView() {
        super.setupView()
        articleContentTextView.setTextWithClickableAnnotations(
            stringId = R.string.article_advice_on_studying,
            onAnnotationClick = { annotationValue: String ->
                when (annotationValue) {
                    "motivational_timer" ->
                        controller?.dispatch(ArticleLinkClicked(MotivationalTimer))
                }
            })
    }
}