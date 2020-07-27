package com.odnovolov.forgetmenot.presentation.screen.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.core.view.isInvisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.searcher.SearchCard
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor.paste
import com.odnovolov.forgetmenot.presentation.screen.search.SearchEvent.BackButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.search.SearchEvent.SearchTextChanged
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.coroutines.launch

class SearchFragment : BaseFragment() {
    init {
        SearchDiScope.reopenIfClosed()
    }

    private var controller: SearchController? = null
    private lateinit var viewModel: SearchViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = SearchDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            initAdapter()
            observeViewModel(isFirstCreated = savedInstanceState == null)
        }
    }

    private fun initAdapter() {
        val adapter = SearchCardAdapter(controller!!)
        cardsRecycler.adapter = adapter
        viewModel.cards.observe { cards: List<SearchCard> -> adapter.items = cards }
    }

    private fun setupView() {
        backButton.run {
            setOnClickListener { controller?.dispatch(BackButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        searchEditText.observeText { newText: String ->
            controller?.dispatch(SearchTextChanged(newText))
        }
        pasteButton.run {
            setOnClickListener { searchEditText.paste() }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        clearButton.run {
            setOnClickListener {
                searchEditText.text.clear()
                searchEditText.showSoftInput()
            }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
    }

    private fun observeViewModel(isFirstCreated: Boolean) {
        with(viewModel) {
            searchDeckName.observe { searchDeckName: String? ->
                searchEditText.hint = (if (searchDeckName == null)
                    getString(R.string.hint_search_in_all_cards) else
                    getString(R.string.hint_search_in_specific_deck, searchDeckName))
            }
            if (isFirstCreated) {
                searchEditText.setText(initialSearchText)
                if (initialSearchText.isEmpty()) {
                    searchEditText.post {
                        searchEditText.showSoftInput()
                    }
                }
            }
            isSearching.observe { isSearching: Boolean ->
                cardsRecycler.isInvisible = isSearching
                progressBar.isInvisible = !isSearching
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hideActionBar()
    }

    override fun onPause() {
        super.onPause()
        searchEditText.hideSoftInput()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            SearchDiScope.close()
        }
    }
}