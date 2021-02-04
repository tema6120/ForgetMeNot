package com.odnovolov.forgetmenot.presentation.screen.home

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.LightPopupWindow
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.observe
import com.odnovolov.forgetmenot.presentation.common.setTooltipTextFromContentDescription
import com.odnovolov.forgetmenot.presentation.common.show
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Criterion.*
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Asc
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Desc
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.*
import kotlinx.android.synthetic.main.fragment_deck_list.*
import kotlinx.android.synthetic.main.item_deck_preview_header.view.*
import kotlinx.android.synthetic.main.popup_deck_filters.view.*
import kotlinx.android.synthetic.main.popup_deck_sorting.view.*
import kotlinx.android.synthetic.main.popup_deck_sorting.view.closeButton
import kotlinx.coroutines.*

class DeckListFragment : BaseFragment() {
    init {
        HomeDiScope.reopenIfClosed()
    }

    private lateinit var viewModel: HomeViewModel
    private var controller: HomeController? = null
    private var deckPreviewAdapter: DeckPreviewAdapter? = null
    private var filtersPopup: PopupWindow? = null
    private var sortingPopup: PopupWindow? = null
    private var resumePauseCoroutineScope: CoroutineScope? = null
    lateinit var scrollListener: RecyclerView.OnScrollListener
    private var filterButton: View? = null
    private var needToShowFiltersPopup = false
    private var needToShowSortingPopup = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_deck_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewCoroutineScope!!.launch {
            val diScope = HomeDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            initDeckPreviewAdapter()
            observeViewModel()
        }
    }

    private fun initDeckPreviewAdapter() {
        val setupHeader: (View) -> Unit = { header: View ->
            filterButton = header.filterButton
            header.filterButton.setOnClickListener {
                showFiltersPopup(anchor = header.filterButton)
            }
            header.sortingButton.setOnClickListener {
                showSortingPopup(anchor = header.sortingButton)
            }
            viewModel.deckSorting.observe { deckSorting: DeckSorting ->
                updateSortingButton(header.sortingButton, deckSorting)
            }
            if (needToShowFiltersPopup) {
                needToShowFiltersPopup = false
                showFiltersPopup(anchor = header.filterButton)
            }
            if (needToShowSortingPopup) {
                needToShowSortingPopup = false
                showSortingPopup(anchor = header.sortingButton)
            }
        }
        deckPreviewAdapter = DeckPreviewAdapter(controller!!, setupHeader)
        decksPreviewRecycler.adapter = deckPreviewAdapter
    }

    private fun showFiltersPopup(anchor: View) {
        requireFiltersPopup().show(anchor = anchor, gravity = Gravity.TOP or Gravity.START)
    }

    private fun showSortingPopup(anchor: View) {
        requireSortingPopup().show(anchor = anchor, gravity = Gravity.TOP or Gravity.END)
    }

    private fun updateSortingButton(
        sortingButton: TextView,
        deckSorting: DeckSorting
    ) {
        sortingButton.text = getString(
            when (deckSorting.criterion) {
                Name -> R.string.sort_by_name
                CreatedAt -> R.string.sort_by_time_created
                LastTestedAt -> R.string.sort_by_time_last_tested
            }
        )
        val directionIconId: Int = when (deckSorting.direction) {
            Asc -> R.drawable.ic_round_arrow_upward_16
            Desc -> R.drawable.ic_round_arrow_downward_16
        }
        sortingButton.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_sorting, 0, directionIconId, 0
        )
    }

    private fun observeViewModel() {
        with(viewModel) {
            decksNotFound.observe { decksNotFound: Boolean ->
                emptyTextView.isVisible = decksNotFound
                progressBar.visibility = View.GONE
            }
            deckSelection.observe { deckSelection: DeckSelection? ->
                deckPreviewAdapter?.deckSelection = deckSelection
                filterButton?.isVisible = deckSelection == null
            }
        }
    }

    private fun requireFiltersPopup(): PopupWindow {
        if (filtersPopup == null) {
            val content = View.inflate(requireContext(), R.layout.popup_deck_filters, null)
                .apply {
                    closeButton.setOnClickListener {
                        filtersPopup?.dismiss()
                    }
                    closeButton.setTooltipTextFromContentDescription()
                    availableForExerciseButton.setOnClickListener {
                        controller?.dispatch(DecksAvailableForExerciseCheckboxClicked)
                    }
                }
            filtersPopup = LightPopupWindow(content)
            viewModel.displayOnlyDecksAvailableForExercise
                .observe { displayOnlyDecksAvailableForExercise: Boolean ->
                    filtersPopup?.contentView?.run {
                        availableForExerciseCheckBox.isChecked =
                            displayOnlyDecksAvailableForExercise
                    }
                }
        }
        return filtersPopup!!
    }

    private fun requireSortingPopup(): PopupWindow {
        if (sortingPopup == null) {
            val content = View.inflate(requireContext(), R.layout.popup_deck_sorting, null)
                .apply {
                    closeButton.setOnClickListener {
                        sortingPopup?.dismiss()
                    }
                    closeButton.setTooltipTextFromContentDescription()
                    sortByNameButton.setOnClickListener {
                        controller?.dispatch(SortByButtonClicked(Name))
                    }
                    sortByTimeCreatedButton.setOnClickListener {
                        controller?.dispatch(SortByButtonClicked(CreatedAt))
                    }
                    sortByTimeLastTestedButton.setOnClickListener {
                        controller?.dispatch(SortByButtonClicked(LastTestedAt))
                    }
                    sortingDirectionButton.setOnClickListener {
                        controller?.dispatch(SortingDirectionButtonClicked)
                    }
                    sortingDirectionButton.setTooltipTextFromContentDescription()
                }
            sortingPopup = LightPopupWindow(content)
            viewModel.deckSorting.observe { deckSorting: DeckSorting ->
                updateSortingPopup(deckSorting)
            }
        }
        return sortingPopup!!
    }

    private fun updateSortingPopup(deckSorting: DeckSorting) {
        sortingPopup?.contentView?.run {
            sortingDirectionButton.setImageResource(
                when (deckSorting.direction) {
                    Asc -> R.drawable.ic_round_arrow_upward_24
                    Desc -> R.drawable.ic_round_arrow_downward_24
                }
            )
            val directionButtonAnchor: View = when (deckSorting.criterion) {
                Name -> sortByNameTextView
                CreatedAt -> sortByTimeCreatedTextView
                LastTestedAt -> sortByTimeLastTestedTextView
            }
            sortingDirectionButton.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topToTop = directionButtonAnchor.id
                bottomToBottom = directionButtonAnchor.id
            }
            sortByNameTextView.isSelected = deckSorting.criterion == Name
            sortByTimeCreatedTextView.isSelected = deckSorting.criterion == CreatedAt
            sortByTimeLastTestedTextView.isSelected = deckSorting.criterion == LastTestedAt
        }
    }

    override fun onResume() {
        super.onResume()
        resumePauseCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        resumePauseCoroutineScope!!.launch {
            val coroutineScope = this
            val diScope = HomeDiScope.getAsync() ?: return@launch
            val viewModel = diScope.viewModel
            with(viewModel) {
                deckListItems.observe(coroutineScope) { deckListItems: List<DeckListItem> ->
                    deckPreviewAdapter?.submitList(deckListItems)
                    progressBar.visibility = View.GONE
                }
            }
        }
        decksPreviewRecycler.addOnScrollListener(scrollListener)
        scrollListener.onScrolled(decksPreviewRecycler, 0, 0)
    }

    override fun onPause() {
        super.onPause()
        resumePauseCoroutineScope!!.cancel()
        resumePauseCoroutineScope = null
        decksPreviewRecycler.removeOnScrollListener(scrollListener)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.run {
            needToShowFiltersPopup = getBoolean(STATE_FILTERS_POPUP, false)
            needToShowSortingPopup = getBoolean(STATE_SORTING_POPUP, false)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val isFiltersPopupShowing = filtersPopup?.isShowing ?: false
        outState.putBoolean(STATE_FILTERS_POPUP, isFiltersPopupShowing)
        val isSortingPopupShowing = sortingPopup?.isShowing ?: false
        outState.putBoolean(STATE_SORTING_POPUP, isSortingPopupShowing)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        decksPreviewRecycler.adapter = null
        deckPreviewAdapter = null
        filtersPopup?.dismiss()
        filtersPopup = null
        sortingPopup?.dismiss()
        sortingPopup = null
        filterButton = null
    }

    companion object {
        private const val STATE_FILTERS_POPUP = "STATE_FILTERS_POPUP"
        private const val STATE_SORTING_POPUP = "STATE_SORTING_POPUP"
    }
}