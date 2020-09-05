package com.odnovolov.forgetmenot.presentation.screen.help.article

import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle
import kotlinx.android.synthetic.main.article.*

class PresetsHelpArticleFragment : BaseHelpArticleFragmentForSimpleUi() {
    override val layoutRes: Int get() = R.layout.article
    override val helpArticle: HelpArticle get() = HelpArticle.Presets

    override fun setupView() {
        articleContentTextView.setText(R.string.article_presets)
    }
}