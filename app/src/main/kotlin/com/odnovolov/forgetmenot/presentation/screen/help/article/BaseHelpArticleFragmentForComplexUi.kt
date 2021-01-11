package com.odnovolov.forgetmenot.presentation.screen.help.article

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.presentation.common.inflateAsync
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticle
import com.odnovolov.forgetmenot.presentation.screen.help.HelpController
import com.odnovolov.forgetmenot.presentation.screen.help.HelpDiScope
import com.odnovolov.forgetmenot.presentation.screen.help.HelpEvent.ArticleOpened
import com.odnovolov.forgetmenot.presentation.screen.help.HelpArticleContainerFragment
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

abstract class BaseHelpArticleFragmentForComplexUi : Fragment() {
    init {
        HelpDiScope.reopenIfClosed()
    }

    protected var viewCoroutineScope: CoroutineScope? = null
    protected abstract val layoutRes: Int
    protected abstract val helpArticle: HelpArticle
    protected var controller: HelpController? = null
    private var isInAndroidViewScope = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isInAndroidViewScope = true
        return inflater.inflateAsync(layoutRes, ::onViewInflated)
    }

    private fun onViewInflated(frame: FrameLayout, inflatedView: View) {
        if (!isInAndroidViewScope) return // if onDestroyView() called earlier than onViewInflated()
        viewCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        viewCoroutineScope!!.launch {
            val diScope = HelpDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            controller!!.dispatch(ArticleOpened(helpArticle))
        }
        (parentFragment as HelpArticleContainerFragment).doWhenDrawerClosed {
            if (isInAndroidViewScope) {
                frame.addView(inflatedView)
                setupView()
            }
        }
    }

    protected open fun setupView() {}

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