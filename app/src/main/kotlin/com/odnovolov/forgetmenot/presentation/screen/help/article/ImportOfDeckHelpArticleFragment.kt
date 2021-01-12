package com.odnovolov.forgetmenot.presentation.screen.help.article

import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.setFont
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle
import kotlinx.android.synthetic.main.article_import_of_deck.*

class ImportOfDeckHelpArticleFragment : BaseHelpArticleFragmentForComplexUi() {
    override val layoutRes: Int get() = R.layout.article_import_of_deck
    override val helpArticle: HelpArticle get() = HelpArticle.ImportOfDeck

    override fun setupView() {
        super.setupView()
        paragraph1.setFont(R.font.nunito_bold)
        paragraph2.setFont(R.font.nunito_bold)
    }
}