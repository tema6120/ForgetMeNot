package com.odnovolov.forgetmenot.presentation.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.FoundCardClicked
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.FoundCardLongClicked
import com.odnovolov.forgetmenot.presentation.screen.search.SelectableSearchCardAdapter
import kotlinx.android.synthetic.main.fragment_found_cards.*
import kotlinx.coroutines.launch

class FoundCardsFragment : BaseFragment() {
    init {
        HomeDiScope.reopenIfClosed()
    }

    private var controller: HomeController? = null
    lateinit var scrollListener: RecyclerView.OnScrollListener
    private lateinit var adapter: SelectableSearchCardAdapter

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
            observeViewModel(diScope.viewModel)
        }
    }

    private fun setupView() {
        adapter = SelectableSearchCardAdapter(
            onCardClicked = { cardId: Long -> controller?.dispatch(FoundCardClicked(cardId)) },
            onCardLongClicked = { cardId: Long -> controller?.dispatch(FoundCardLongClicked(cardId)) },
        )
        cardsRecycler.adapter = adapter
    }

    private fun observeViewModel(viewModel: HomeViewModel) {
        with(viewModel) {
            foundCards.observe(adapter::submitList)
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