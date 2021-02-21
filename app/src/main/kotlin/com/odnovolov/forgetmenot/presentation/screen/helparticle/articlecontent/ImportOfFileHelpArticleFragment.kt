package com.odnovolov.forgetmenot.presentation.screen.helparticle.articlecontent

import android.text.method.LinkMovementMethod
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle
import kotlinx.android.synthetic.main.article_import_of_file.*

class ImportOfFileHelpArticleFragment : BaseHelpArticleFragmentForComplexUi() {
    override val layoutRes: Int get() = R.layout.article_import_of_file
    override val helpArticle: HelpArticle get() = HelpArticle.ImportOfFile

    override fun setupView() {
        super.setupView()
        dsvFormatDescriptionTextView.movementMethod = LinkMovementMethod.getInstance()
    }
}