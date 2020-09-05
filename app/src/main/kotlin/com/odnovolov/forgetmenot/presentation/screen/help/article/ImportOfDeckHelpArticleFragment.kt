package com.odnovolov.forgetmenot.presentation.screen.help.article

import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle

class ImportOfDeckHelpArticleFragment : BaseHelpArticleFragmentForComplexUi() {
    override val layoutRes: Int get() = R.layout.article_import_of_deck
    override val helpArticle: HelpArticle get() = HelpArticle.ImportOfDeck
}