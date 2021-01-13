package com.odnovolov.forgetmenot.presentation.screen.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.screen.help.HelpEvent.ArticleItemClicked
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticle
import kotlinx.android.synthetic.main.fragment_help.*
import kotlinx.android.synthetic.main.fragment_help.appBar
import kotlinx.android.synthetic.main.fragment_help.backButton
import kotlinx.android.synthetic.main.fragment_help.contentScrollView
import kotlinx.coroutines.launch

class HelpFragment : BaseFragment() {
    init {
        HelpDiScope.reopenIfClosed()
    }

    private var controller: HelpController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_help, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = HelpDiScope.getAsync() ?: return@launch
            controller = diScope.controller
        }
    }

    private fun setupView() {
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        helpArticleRecyclerView.adapter = HelpArticleAdapter(
            onItemClicked = { helpArticle: HelpArticle ->
                controller?.dispatch(ArticleItemClicked(helpArticle))
            }
        )
    }

    override fun onResume() {
        super.onResume()
        appBar.post { appBar.isActivated = contentScrollView.canScrollVertically(-1) }
        contentScrollView.viewTreeObserver.addOnScrollChangedListener(scrollListener)
    }

    override fun onPause() {
        super.onPause()
        contentScrollView.viewTreeObserver.removeOnScrollChangedListener(scrollListener)
    }

    private val scrollListener = ViewTreeObserver.OnScrollChangedListener {
        val canScrollUp = contentScrollView.canScrollVertically(-1)
        if (appBar.isActivated != canScrollUp) {
            appBar.isActivated = canScrollUp
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            HelpDiScope.close()
        }
    }
}