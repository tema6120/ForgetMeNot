package com.odnovolov.forgetmenot.presentation.screen.helparticle.articlecontent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.inflateAsync
import com.odnovolov.forgetmenot.presentation.common.setFont
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleController
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleDiScope
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleEvent.ArticleOpened
import kotlinx.android.synthetic.main.article.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

abstract class BaseHelpArticleFragmentForSimpleUi : Fragment() {
    init {
        HelpArticleDiScope.reopenIfClosed()
    }

    protected var viewCoroutineScope: CoroutineScope? = null
    protected abstract val layoutRes: Int
    protected abstract val helpArticle: HelpArticle
    protected var controller: HelpArticleController? = null
    private var isInAndroidViewScope = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isInAndroidViewScope = true
        return inflater.inflateAsync(layoutRes, ::onViewInflated)
    }

    private fun onViewInflated() {
        if (!isInAndroidViewScope) return // if onDestroyView() called earlier than onViewInflated()
        viewCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        viewCoroutineScope!!.launch {
            val diScope = HelpArticleDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            controller!!.dispatch(ArticleOpened(helpArticle))
        }
        setupView()
    }

    protected open fun setupView() {
        articleContentTextView.setFont(R.font.nunito_bold)
    }

    protected inline fun <T> Flow<T>.observe(crossinline onEach: (value: T) -> Unit) {
        viewCoroutineScope!!.launch {
            collect {
                if (isActive) {
                    onEach(it)
                }
            }
        }
    }

    override fun onDestroyView() {
        viewCoroutineScope?.cancel()
        viewCoroutineScope = null
        isInAndroidViewScope = false
        super.onDestroyView()
    }
}