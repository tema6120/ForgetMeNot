package com.odnovolov.forgetmenot.presentation.screen.helparticle.articlecontent

import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.setTextWithClickableAnnotations
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle.MotivationalTimer
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleEvent.ArticleLinkClicked
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