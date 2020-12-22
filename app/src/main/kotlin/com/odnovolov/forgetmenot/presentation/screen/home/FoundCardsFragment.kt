package com.odnovolov.forgetmenot.presentation.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.searcher.SearchCard
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.FoundCardClicked
import kotlinx.android.synthetic.main.fragment_found_cards.*
import kotlinx.coroutines.launch

class FoundCardsFragment : BaseFragment() {
    init {
        HomeDiScope.reopenIfClosed()
    }

    private lateinit var viewModel: HomeViewModel
    private var controller: HomeController? = null
    lateinit var scrollListener: RecyclerView.OnScrollListener

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
            observeViewModel()
        }
    }

    private fun setupView() {
        val onCardClicked: (SearchCard) -> Unit = { searchCard: SearchCard ->
            controller?.dispatch(FoundCardClicked(searchCard))
        }
        cardsRecycler.adapter = FoundCardAdapter(onCardClicked)
    }

    private fun observeViewModel() {
        with(viewModel) {
            areCardsBeingSearched.observe { isSearching: Boolean ->
                cardsRecycler.isInvisible = isSearching
                searchingCardsProgressBar.isVisible = isSearching
            }
            foundCards.observe { cards: List<SearchCard> ->
                (cardsRecycler.adapter as FoundCardAdapter).items = cards
            }
            cardsNotFound.observe { cardsNotFound: Boolean ->
                emptyTextView.isVisible = cardsNotFound
            }
        }
    }

    override fun onResume() {
        super.onResume()
        cardsRecycler.addOnScrollListener(scrollListener)
        scrollListener.onScrolled(cardsRecycler, 0, 0)
    }

    override fun onPause() {
        super.onPause()
        cardsRecycler.removeOnScrollListener(scrollListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cardsRecycler.adapter = null
    }
}