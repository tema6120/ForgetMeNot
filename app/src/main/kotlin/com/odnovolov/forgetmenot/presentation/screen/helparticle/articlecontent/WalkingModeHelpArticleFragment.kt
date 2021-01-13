package com.odnovolov.forgetmenot.presentation.screen.helparticle.articlecontent

import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.setTextWithClickableAnnotations
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle
import kotlinx.android.synthetic.main.article.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WalkingModeHelpArticleFragment : BaseHelpArticleFragmentForSimpleUi() {
    override val layoutRes: Int get() = R.layout.article
    override val helpArticle: HelpArticle get() = HelpArticle.WalkingMode
    private var navigator: Navigator? = null

    override fun setupView() {
        super.setupView()
        viewCoroutineScope!!.launch {
            val appDiScope = withContext(businessLogicThread) {
                AppDiScope.get()
            }
            navigator = appDiScope.navigator
        }
        articleContentTextView.setTextWithClickableAnnotations(
            stringId = R.string.article_walking_mode,
            onAnnotationClick = { annotationValue: String ->
                when (annotationValue) {
                    "walking_mode_settings" ->
                        navigator?.navigateToWalkingModeSettingsFromHelpArticle()
                }
            })
    }
}