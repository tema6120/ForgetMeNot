package com.odnovolov.forgetmenot.presentation.screen.deckchooser

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.PopupWindow
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserEvent.*
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserScreenState.Purpose.*
import com.odnovolov.forgetmenot.presentation.screen.home.DeckListItem
import com.odnovolov.forgetmenot.presentation.screen.home.DeckPreviewAdapter
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Criterion.*
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Asc
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Desc
import kotlinx.android.synthetic.main.fragment_deck_chooser.*
import kotlinx.android.synthetic.main.item_deck_preview_header.view.*
import kotlinx.android.synthetic.main.popup_deck_sorting.view.*
import kotlinx.coroutines.launch

class DeckChooserFragment : BaseFragment() {
    init {
        DeckChooserDiScope.reopenIfClosed()
    }

    private var controller: DeckChooserController? = null
    private lateinit var viewModel: DeckChooserViewModel
    private var deckPreviewAdapter: DeckPreviewAdapter? = null
    private var sortingPopup: PopupWindow? = null
    private var needToShowSortingPopup = false
    private var backPressInterceptor: MainActivity.BackPressInterceptor? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setStatusBarColor(requireActivity(), R.color.colorAccent)
        return inflater.inflate(R.layout.fragment_deck_chooser, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = DeckChooserDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            initDeckPreviewAdapter()
            observeViewModel()
        }
    }

    private fun setupView() {
        cancelButton.setOnClickListener {
            controller?.dispatch(CancelButtonClicked)
        }
        searchEditText.observeText { newText: String ->
            controller?.dispatch(SearchTextChanged(newText))
        }
        addDeckButton.setOnClickListener {
            controller?.dispatch(AddDeckButtonClicked)
        }
    }

    private fun initDeckPreviewAdapter() {
        val setupHeader: (View) -> Unit = { header: View ->
            header.updateLayoutParams<MarginLayoutParams> { topMargin = 8.dp }
            header.filterButton.isVisible = false
            header.sortingButton.setOnClickListener {
                showSortingPopup(anchor = header.sortingButton)
            }
            viewModel.deckSorting.observe { deckSorting: DeckSorting ->
                updateSortingButton(header.sortingButton, deckSorting)
            }
            if (needToShowSortingPopup) {
                needToShowSortingPopup = false
                showSortingPopup(anchor = header.sortingButton)
            }
        }
        deckPreviewAdapter = DeckPreviewAdapter(
            setupHeader,
            onDeckButtonClicked = { deckId -> controller?.dispatch(DeckButtonClicked(deckId)) }
        )
        decksPreviewRecycler.adapter = deckPreviewAdapter
    }

    private fun observeViewModel() {
        with(viewModel) {
            screenTitleTextView.setText(
                when (purpose) {
                    ToImportCards -> R.string.screen_title_choose_a_deck_to_import_cards_to
                    ToMergeInto -> R.string.screen_title_choose_a_deck_to_merge_into
                    ToMoveCard -> R.string.screen_title_choose_a_deck_to_move_card
                    ToCopyCard -> R.string.screen_title_choose_a_deck_to_copy_card
                    ToMoveCardsInDeckEditor, ToMoveCardsInSearch, ToMoveCardsInHomeSearch ->
                        R.string.screen_title_choose_a_deck_to_move_cards
                    ToCopyCardsInDeckEditor, ToCopyCardsInSearch, ToCopyCardsInHomeSearch ->
                        R.string.screen_title_choose_a_deck_to_copy_cards
                }
            )
            deckListItems.observe { deckListItems: List<DeckListItem> ->
                deckPreviewAdapter?.submitList(deckListItems)
                progressBar.visibility = View.GONE
            }
            decksNotFound.observe { decksNotFound: Boolean ->
                emptyTextView.isVisible = decksNotFound
                progressBar.visibility = View.GONE
            }
            if (isAddDeckButtonVisible) {
                addDeckButton.show()
            } else {
                addDeckButton.hide()
            }
        }
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
                FrequencyOfUse -> R.string.sort_by_frequency_of_use
            }
        )
        val directionIconId: Int = when (deckSorting.direction) {
            Asc -> R.drawable.ic_round_arrow_upward_12
            Desc -> R.drawable.ic_round_arrow_downward_12
        }
        sortingButton.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_sorting_12, 0, directionIconId, 0
        )
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
                    sortByFrequencyOfUseButton.setOnClickListener {
                        controller?.dispatch(SortByButtonClicked(FrequencyOfUse))
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
                FrequencyOfUse -> sortByFrequencyOfUseTextView
            }
            sortingDirectionButton.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topToTop = directionButtonAnchor.id
                bottomToBottom = directionButtonAnchor.id
            }
            sortByNameTextView.isSelected = deckSorting.criterion == Name
            sortByTimeCreatedTextView.isSelected = deckSorting.criterion == CreatedAt
            sortByTimeLastTestedTextView.isSelected = deckSorting.criterion == LastTestedAt
            sortByFrequencyOfUseTextView.isSelected = deckSorting.criterion == FrequencyOfUse
        }
    }

    private fun cancelSearch() {
        searchEditText.text.clear()
        searchEditText.clearFocus()
    }

    override fun onResume() {
        super.onResume()
        backPressInterceptor = MainActivity.BackPressInterceptor {
            when {
                searchEditText.hasFocus() -> {
                    cancelSearch()
                    true
                }
                else -> {
                    false
                }
            }
        }
        (activity as MainActivity).registerBackPressInterceptor(backPressInterceptor!!)
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).unregisterBackPressInterceptor(backPressInterceptor!!)
        backPressInterceptor = null
        searchEditText.hideSoftInput()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.run {
            needToShowSortingPopup = getBoolean(STATE_SORTING_POPUP, false)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val isSortingPopupShowing = sortingPopup?.isShowing ?: false
        outState.putBoolean(STATE_SORTING_POPUP, isSortingPopupShowing)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        decksPreviewRecycler.adapter = null
        deckPreviewAdapter = null
        sortingPopup?.dismiss()
        sortingPopup = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            DeckChooserDiScope.close()
        }
    }

    companion object {
        private const val STATE_SORTING_POPUP = "STATE_SORTING_POPUP_IN_DECK_CHOOSER"
    }
}