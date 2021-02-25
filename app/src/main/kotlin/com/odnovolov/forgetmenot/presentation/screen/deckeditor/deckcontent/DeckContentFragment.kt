package com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_deck_content.*
import kotlinx.coroutines.launch

class DeckContentFragment : BaseFragment() {
    init {
        DeckContentDiScope.reopenIfClosed()
    }

    private var controller: DeckContentController? = null
    private lateinit var viewModel: DeckContentViewModel
    private var isInflated = false
    lateinit var scrollListener: OnScrollListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return if (savedInstanceState == null) {
            inflater.inflateAsync(R.layout.fragment_deck_content, ::onViewInflated)
        } else {
            inflater.inflate(R.layout.fragment_deck_content, container, false)
        }
    }

    private fun onViewInflated() {
        if (viewCoroutineScope != null) {
            isInflated = true
            setupIfReady()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            isInflated = true
        }
        viewCoroutineScope!!.launch {
            val diScope = DeckContentDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            setupIfReady()
        }
    }

    private fun setupIfReady() {
        if (viewCoroutineScope == null || controller == null || !isInflated) return
        val adapter = CardOverviewAdapter(controller!!)
        cardsRecycler.adapter = adapter
        viewModel.cards.observe(adapter::submitItems)
        cardsRecycler.addOnScrollListener(scrollListener)
    }

    override fun onDestroyView() {
        cardsRecycler.removeOnScrollListener(scrollListener)
        super.onDestroyView()
        isInflated = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            DeckContentDiScope.close()
        }
    }
}