package com.odnovolov.forgetmenot.presentation.screen.help.article

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.setTextWithClickableAnnotations
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle.AdviceOnStudying
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle.MotivationalTimer
import com.odnovolov.forgetmenot.presentation.screen.help.HelpDiScope
import com.odnovolov.forgetmenot.presentation.screen.help.HelpEvent.ArticleLinkClicked
import com.odnovolov.forgetmenot.presentation.screen.help.HelpEvent.ArticleOpened
import kotlinx.coroutines.launch

class AdviceOnStudyingHelpArticleFragment : BaseFragment() {
    init {
        HelpDiScope.reopenIfClosed()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.article, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewCoroutineScope!!.launch {
            val diScope = HelpDiScope.getAsync() ?: return@launch
            val controller = diScope.controller
            view as TextView
            view.setTextWithClickableAnnotations(
                stringId = R.string.article_advice_on_studying,
                onAnnotationClick = { annotationValue: String ->
                    when (annotationValue) {
                        "motivational_timer" ->
                            controller.dispatch(ArticleLinkClicked(MotivationalTimer))
                    }
                })
        }
    }

    override fun onResume() {
        super.onResume()
        viewCoroutineScope!!.launch {
            val diScope = HelpDiScope.getAsync() ?: return@launch
            diScope.controller.dispatch(ArticleOpened(AdviceOnStudying))
        }
    }
}