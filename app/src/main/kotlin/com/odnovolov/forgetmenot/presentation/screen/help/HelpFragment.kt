package com.odnovolov.forgetmenot.presentation.screen.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.firstBlocking
import com.odnovolov.forgetmenot.presentation.common.hideActionBar
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.screen.help.HelpController.Command.OpenArticle
import com.odnovolov.forgetmenot.presentation.screen.help.HelpEvent.ArticleClickedInTableOfContents
import kotlinx.android.synthetic.main.fragment_help.*
import kotlinx.coroutines.launch

class HelpFragment : BaseFragment() {
    init {
        HelpDiScope.reopenIfClosed()
    }

    private var controller: HelpController? = null
    private lateinit var viewModel: HelpViewModel
    private var needToResetScrollView = false

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
            viewModel = diScope.viewModel
            initAdapter()
            observeViewModel(isFirstCreated = savedInstanceState == null)
            controller!!.commands.observe(::executeCommand)
        }
    }

    private fun observeViewModel(isFirstCreated: Boolean) {
        if (isFirstCreated) {
            openArticle(viewModel.currentArticle.firstBlocking(), needToClearBackStack = true)
        }
        viewModel.currentArticle.observe { article: HelpArticle ->
            articleTitleTextView.setText(article.titleId)
        }
    }

    private fun executeCommand(command: HelpController.Command) {
        when (command) {
            is OpenArticle -> openArticle(command.article, command.needToClearBackStack)
        }
    }

    private fun setupView() {
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        showTableOfContentsButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }
    }

    private fun initAdapter() {
        val onItemSelected: (HelpArticle) -> Unit = { helpArticle: HelpArticle ->
            drawerLayout.closeDrawer(GravityCompat.END)
            controller?.dispatch(ArticleClickedInTableOfContents(helpArticle))
        }
        val adapter = HelpArticleAdapter(onItemSelected)
        tableOfContentsRecycler.adapter = adapter
        viewModel.articleItems.observe(adapter::submitList)
    }

    private fun openArticle(helpArticle: HelpArticle, needToClearBackStack: Boolean) {
        if (needToClearBackStack) {
            childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        childFragmentManager.beginTransaction()
            .replace(R.id.articleFrame, helpArticle.createFragment())
            .apply { if (!needToClearBackStack) addToBackStack(null) }
            .commit()
        needToResetScrollView = true
    }

    override fun onResume() {
        super.onResume()
        hideActionBar()
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        if (needToResetScrollView) {
            articleScrollView.scrollTo(0, 0)
            needToResetScrollView = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            HelpDiScope.close()
        }
    }
}