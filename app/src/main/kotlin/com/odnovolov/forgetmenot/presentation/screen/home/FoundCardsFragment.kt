package com.odnovolov.forgetmenot.presentation.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.searcher.SearchCard
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_found_cards.*
import kotlinx.coroutines.launch

class FoundCardsFragment : BaseFragment() {
    init {
        HomeDiScope.reopenIfClosed()
    }

    private lateinit var viewModel: HomeViewModel
    private var controller: HomeController? = null
    private lateinit var adapter: FoundCardAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_found_cards, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = HomeDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            initAdapter()
            observeViewModel()
        }
    }

    private fun setupView() {
        cardsRecycler.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val canScrollUp = cardsRecycler.canScrollVertically(-1)
                divider.isVisible = canScrollUp
            }
        })
    }

    private fun initAdapter() {
        val onCardClicked: (SearchCard) -> Unit = {

        }
        adapter = FoundCardAdapter(onCardClicked)
        cardsRecycler.adapter = adapter
    }

    private fun observeViewModel() {
        with(viewModel) {
            isSearching.observe { isSearching: Boolean ->
                cardsRecycler.isInvisible = isSearching
                searchingCardsProgressBar.isInvisible = !isSearching
            }
            foundCards.observe { cards: List<SearchCard> ->
                adapter.items = cards
            }
            cardsNotFound.observe { cardsNotFound: Boolean ->
                emptyTextView.isVisible = cardsNotFound
            }
        }
    }
}