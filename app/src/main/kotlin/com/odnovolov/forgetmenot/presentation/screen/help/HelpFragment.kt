package com.odnovolov.forgetmenot.presentation.screen.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.hideActionBar
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.screen.help.HelpEvent.BackButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.help.HelpEvent.HelpArticleSelected
import kotlinx.android.synthetic.main.fragment_help.*
import kotlinx.coroutines.launch

class HelpFragment : BaseFragment() {
    init {
        HelpDiScope.reopenIfClosed()
    }

    private var controller: HelpController? = null
    private lateinit var viewModel: HelpViewModel

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
            observeViewModel()
        }
    }

    private fun setupView() {
        backButton.setOnClickListener {
            controller?.dispatch(BackButtonClicked)
        }
        showTableOfContentsButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }
    }

    private fun initAdapter() {
        val onItemSelected: (HelpArticle) -> Unit = { helpArticle: HelpArticle ->
            drawerLayout.closeDrawer(GravityCompat.END)
            controller?.dispatch(HelpArticleSelected(helpArticle))
        }
        val adapter = HelpArticleAdapter(onItemSelected)
        tableOfContentsRecycler.adapter = adapter
        viewModel.helpArticleItems.observe(adapter::submitList)
    }

    private fun observeViewModel() {
        with(viewModel) {
            currentHelpArticle.observe { currentHelpArticle: HelpArticle ->
                childFragmentManager.beginTransaction()
                    .replace(R.id.articleFrame, currentHelpArticle.createFragment())
                    .commit()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideActionBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tableOfContentsRecycler.adapter = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            HelpDiScope.close()
        }
    }
}