package com.odnovolov.forgetmenot.presentation.screen.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.searcher.SearchCard
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.hideSoftInput
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor.paste
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
        viewModel.foundCards.observe { cards: List<SearchCard> ->
            adapter.items = cards
        }
    }

    private fun setupView() {
        backButton.run {
            setOnClickListener { activity?.onBackPressed() }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        searchEditText.observeText { newText: String ->
            controller?.dispatch(SearchTextChanged(newText))
            updatePasteClearButton()
        }
    }

    private fun updatePasteClearButton() {
        val hasSearchText = searchEditText.text.isNotEmpty()
        with(pasteClearButton) {
            setImageResource(
                if (hasSearchText)
                    R.drawable.ic_clear_colored else
                    R.drawable.ic_paste_colored
            )
            setOnClickListener {
                if (hasSearchText) {
                    searchEditText.text.clear()
                    searchEditText.showSoftInput()
                } else {
                    searchEditText.paste()
                    searchEditText.requestFocus()
                }
            }
            contentDescription = getString(
                if (hasSearchText)
                    R.string.description_clear_button else
                    R.string.description_paste_button
            )
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
                progressBar.isVisible = isSearching
            }
            cardsNotFound.observe { cardsNotFound: Boolean ->
                emptyTextView.isVisible = cardsNotFound
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appBar.post { appBar.isActivated = cardsRecycler.canScrollVertically(-1) }
        cardsRecycler.addOnScrollListener(scrollListener)
    }

    override fun onPause() {
        super.onPause()
        cardsRecycler.removeOnScrollListener(scrollListener)
        searchEditText.hideSoftInput()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            SearchDiScope.close()
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val canScrollUp = recyclerView.canScrollVertically(-1)
            if (appBar.isActivated != canScrollUp) {
                appBar.isActivated = canScrollUp
            }
        }
    }
}