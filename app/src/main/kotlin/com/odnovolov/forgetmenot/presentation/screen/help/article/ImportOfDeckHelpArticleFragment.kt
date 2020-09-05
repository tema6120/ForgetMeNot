package com.odnovolov.forgetmenot.presentation.screen.help.article

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle.ImportOfDeck
import com.odnovolov.forgetmenot.presentation.screen.help.HelpDiScope
import com.odnovolov.forgetmenot.presentation.screen.help.HelpEvent.ArticleOpened
import kotlinx.coroutines.launch

class ImportOfDeckHelpArticleFragment : BaseFragment() {
    init {
        HelpDiScope.reopenIfClosed()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.article_import_of_deck, container, false)
    }

    override fun onResume() {
        super.onResume()
        viewCoroutineScope!!.launch {
            val diScope = HelpDiScope.getAsync() ?: return@launch
            diScope.controller.dispatch(ArticleOpened(ImportOfDeck))
        }
    }
}