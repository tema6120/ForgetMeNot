package com.odnovolov.forgetmenot.presentation.screen.deckchooser

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.*
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.DeckList
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserEvent.*
import com.odnovolov.forgetmenot.presentation.screen.deckchooser.DeckChooserScreenState.Purpose.*
import com.odnovolov.forgetmenot.presentation.screen.home.*
import com.odnovolov.forgetmenot.presentation.screen.home.DeckReviewPreference.Companion.DEFAULT_DECK_LIST_COLOR
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Criterion.*
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Asc
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Desc
import kotlinx.android.synthetic.main.fragment_deck_chooser.*
import kotlinx.android.synthetic.main.item_deck_preview_header_in_deck_chooser.view.*
import kotlinx.android.synthetic.main.popup_deck_list_in_deck_chooser.view.*
import kotlinx.android.synthetic.main.popup_deck_sorting.view.*
import kotlinx.android.synthetic.main.popup_deck_sorting.view.closeButton
import kotlinx.coroutines.launch

class DeckChooserFragment : BaseFragment() {
    init {
        DeckChooserDiScope.reopenIfClosed()
    }

    private var controller: DeckChooserController? = null
    private lateinit var viewModel: DeckChooserViewModel
    private var deckListsPopup: PopupWindow? = null
    private var sortingPopup: PopupWindow? = null
    private var needToShowdeckListsPopup = false
    private var needToShowSortingPopup = false
    private val selectableDeckListAdapter = SelectableDeckListAdapter(
        onDeckListButtonClicked = { deckListId: Long? ->
            controller?.dispatch(DeckListSelected(deckListId))
            deckListsPopup?.dismiss()
        }
    )
    private var deckPreviewAdapter: DeckPreviewAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setStatusBarColor(requireActivity(), R.color.selection_toolbar)
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
        val setupHeader: (ViewGroup) -> View = { parent: ViewGroup ->
            val header: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_deck_preview_header_in_deck_chooser, parent, false)
            header.deckListButton.setOnClickListener {
                showDeckListsPopup(anchor = header.deckListButton)
            }
            header.sortingButton.setOnClickListener {
                showSortingPopup(anchor = header.sortingButton)
            }
            subscribeHeaderItemToViewModel(header)
            if (needToShowdeckListsPopup) {
                needToShowdeckListsPopup = false
                showDeckListsPopup(anchor = header.deckListButton)
            }
            if (needToShowSortingPopup) {
                needToShowSortingPopup = false
                showSortingPopup(anchor = header.sortingButton)
            }
            header
        }
        deckPreviewAdapter = DeckPreviewAdapter(
            setupHeader,
            onDeckButtonClicked = { deckId -> controller?.dispatch(DeckButtonClicked(deckId)) }
        )
        decksPreviewRecycler.adapter = deckPreviewAdapter
    }

    private fun showDeckListsPopup(anchor: View) {
        requireDeckListsPopup().show(anchor, gravity = Gravity.TOP or Gravity.START)
    }

    private fun requireDeckListsPopup(): PopupWindow {
        if (deckListsPopup == null) {
            val content: View =
                View.inflate(requireContext(), R.layout.popup_deck_list_in_deck_chooser, null)
                    .apply {
                        closeButton.setOnClickListener {
                            deckListsPopup?.dismiss()
                        }
                        closeButton.setTooltipTextFromContentDescription()
                        deckListRecycler.adapter = selectableDeckListAdapter
                    }
            val scrollListener = object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val canScrollUp = recyclerView.canScrollVertically(-1)
                    if (content.divider.isInvisible == canScrollUp) {
                        content.divider.isInvisible = !canScrollUp
                    }
                }
            }
            content.deckListRecycler.addOnScrollListener(scrollListener)
            deckListsPopup = LightPopupWindow(content).apply {
                width = 250.dp
                setOnDismissListener {
                    content.deckListRecycler.removeOnScrollListener(scrollListener)
                }
            }
            subscribeDeckListsPopupToViewModel(content)
        }
        return deckListsPopup!!
    }

    private fun subscribeDeckListsPopupToViewModel(popupContentView: View) {
        with(viewModel) {
            selectableDeckLists.observe { deckList: List<SelectableDeckList> ->
                selectableDeckListAdapter.items = deckList
            }
        }
    }

    private fun showSortingPopup(anchor: View) {
        requireSortingPopup().show(anchor = anchor, gravity = Gravity.TOP or Gravity.END)
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
                    sortByTaskButton.setOnClickListener {
                        controller?.dispatch(SortByButtonClicked(Task))
                    }
                    sortingDirectionButton.setOnClickListener {
                        controller?.dispatch(SortingDirectionButtonClicked)
                    }
                    sortingDirectionButton.setTooltipTextFromContentDescription()
                    newDecksFirstButton.setOnClickListener {
                        controller?.dispatch(NewDecksFirstCheckboxClicked)
                    }
                }
            sortingPopup = LightPopupWindow(content)
            subscribeSortingPopupToViewModel(content)
        }
        return sortingPopup!!
    }

    private fun subscribeSortingPopupToViewModel(popupContentView: View) {
        viewModel.deckSorting.observe { deckSorting: DeckSorting ->
            popupContentView.run {
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
                    Task -> sortByTaskTextView
                }
                sortingDirectionButton.updateLayoutParams<LayoutParams> {
                    topToTop = directionButtonAnchor.id
                    bottomToBottom = directionButtonAnchor.id
                }
                sortByNameTextView.isSelected = deckSorting.criterion == Name
                sortByTimeCreatedTextView.isSelected = deckSorting.criterion == CreatedAt
                sortByTimeLastTestedTextView.isSelected = deckSorting.criterion == LastTestedAt
                sortByFrequencyOfUseTextView.isSelected = deckSorting.criterion == FrequencyOfUse
                sortByTaskTextView.isSelected = deckSorting.criterion == Task
                newDecksFirstCheckBox.isChecked = deckSorting.newDecksFirst
            }
        }
    }

    private fun subscribeHeaderItemToViewModel(header: View) {
        with(viewModel) {
            currentDeckList.observe { deckList: DeckList? ->
                header.deckListButton.run {
                    text = deckList?.name ?: getString(R.string.deck_list_title_all_decks)
                    val deckListColor = deckList?.color ?: DEFAULT_DECK_LIST_COLOR
                    val deckListIcon: Drawable =
                        DeckListDrawableGenerator.generateIcon(deckListColor)
                    val expandIcon: Drawable = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_expand_more_thick_12
                    )!!
                    expandIcon.setTint(ContextCompat.getColor(context, R.color.text_medium_emphasis))
                    expandIcon.setBounds(
                        0,
                        0,
                        expandIcon.intrinsicWidth,
                        expandIcon.intrinsicHeight
                    )
                    setCompoundDrawablesRelative(deckListIcon, null, expandIcon, null)
                }
            }
            deckSorting.observe { deckSorting: DeckSorting ->
                header.sortingButton.text = getString(
                    when (deckSorting.criterion) {
                        Name -> R.string.sort_by_name
                        CreatedAt -> R.string.sort_by_time_created
                        LastTestedAt -> R.string.sort_by_time_last_tested
                        FrequencyOfUse -> R.string.sort_by_frequency_of_use
                        Task -> R.string.sort_by_task
                    }
                )
                val directionIconId: Int = when (deckSorting.direction) {
                    Asc -> R.drawable.ic_round_arrow_upward_12
                    Desc -> R.drawable.ic_round_arrow_downward_12
                }
                header.sortingButton.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_sorting_12, 0, directionIconId, 0
                )
            }
        }
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

    private fun cancelSearch() {
        searchEditText.text.clear()
        searchEditText.clearFocus()
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).registerBackPressInterceptor(backPressInterceptor)
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).unregisterBackPressInterceptor(backPressInterceptor)
        searchEditText.hideSoftInput()
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.run {
            needToShowSortingPopup = getBoolean(STATE_SORTING_POPUP, false)
            needToShowdeckListsPopup = getBoolean(STATE_DECK_LISTS_POPUP, false)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val isDeckListsPopupShowing = deckListsPopup?.isShowing ?: false
        outState.putBoolean(STATE_DECK_LISTS_POPUP, isDeckListsPopupShowing)
        val isSortingPopupShowing = sortingPopup?.isShowing ?: false
        outState.putBoolean(STATE_SORTING_POPUP, isSortingPopupShowing)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        decksPreviewRecycler.adapter = null
        deckListsPopup?.dismiss()
        deckListsPopup = null
        sortingPopup?.dismiss()
        sortingPopup = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            DeckChooserDiScope.close()
        }
    }

    private val backPressInterceptor = MainActivity.BackPressInterceptor {
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

    companion object {
        private const val STATE_DECK_LISTS_POPUP = "STATE_DECK_LISTS_POPUP"
        private const val STATE_SORTING_POPUP = "STATE_SORTING_POPUP_IN_DECK_CHOOSER"
    }
}