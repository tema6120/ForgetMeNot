package com.odnovolov.forgetmenot.presentation.screen.search

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.google.android.material.snackbar.Snackbar
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor.paste
import com.odnovolov.forgetmenot.presentation.screen.search.SearchController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.search.SearchEvent.*
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.toolbar_item_selection.*
import kotlinx.coroutines.launch
import kotlin.math.abs

class SearchFragment : BaseFragment() {
    init {
        SearchDiScope.reopenIfClosed()
    }

    private var controller: SearchController? = null
    private var isSelectionMode = false
    private var isAntiJumpingViewActivated = false
    private var lastShownSnackbar: Snackbar? = null
    private lateinit var adapter: SelectableSearchCardAdapter

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
            observeViewModel(diScope.viewModel)
            controller!!.commands.observe(::executeCommand)
            if (searchEditText.text.isNotEmpty()) {
                controller!!.dispatch(SearchTextChanged(searchEditText.text.toString()))
            }
        }
    }

    private fun setupView() {
        setupSearchFrame()
        setupSelectionToolbar()
        initAdapter()
    }

    private fun setupSearchFrame() {
        backButton.run {
            setOnClickListener { activity?.onBackPressed() }
            setTooltipTextFromContentDescription()
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
                    R.drawable.ic_round_clear_24 else
                    R.drawable.ic_content_paste_24
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
            setTooltipTextFromContentDescription()
        }
    }

    private fun setupSelectionToolbar() {
        cancelSelectionButton.run {
            setOnClickListener { controller?.dispatch(CardSelectionWasCancelled) }
            setTooltipTextFromContentDescription()
        }
        selectAllButton.run {
            setOnClickListener { controller?.dispatch(SelectAllCardsButtonClicked) }
            setTooltipTextFromContentDescription()
        }
        removeOptionItem.run {
            setOnClickListener { controller?.dispatch(RemoveCardsCardSelectionOptionWasSelected) }
            setTooltipTextFromContentDescription()
        }
        moreOptionsButton.run {
            setOnClickListener {
                if (controller != null) {
                    CardSelectionOptionsBottomSheet()
                        .show(childFragmentManager, "CardSelectionOptionsBottomSheet")
                }
            }
            setTooltipTextFromContentDescription()
        }
    }

    private fun initAdapter() {
        adapter = SelectableSearchCardAdapter(
            onCardClicked = { cardId: Long -> controller?.dispatch(CardClicked(cardId)) },
            onCardLongClicked = { cardId: Long -> controller?.dispatch(CardLongClicked(cardId)) }
        )
        cardsRecycler.adapter = adapter
    }

    private fun observeViewModel(viewModel: SearchViewModel) {
        with(viewModel) {
            foundCards.observe(adapter::items::set)
            searchDeckName.observe { searchDeckName: String? ->
                searchEditText.hint = (if (searchDeckName == null)
                    getString(R.string.hint_search_in_all_cards) else
                    getString(R.string.hint_search_in_specific_deck, searchDeckName))
            }
            isSearching.observe { isSearching: Boolean ->
                progressBar.isInvisible = !isSearching
            }
            if (isViewFirstCreated) {
                if (initialSearchText.isEmpty()) {
                    searchEditText.post {
                        searchEditText.showSoftInput()
                    }
                } else {
                    searchEditText.setText(initialSearchText)
                }
            }
            cardsNotFound.observe { cardsNotFound: Boolean ->
                emptyTextView.isVisible = cardsNotFound
            }
            isSelectionMode.observe { isSelectionMode: Boolean ->
                this@SearchFragment.isSelectionMode = isSelectionMode
                preventCardItemsJumping()
                updateStatusBarColor()
                updateAppbarItemsVisibility()
            }
            numberOfSelectedCards.observe { numberOfSelectedCards: Int ->
                numberOfSelectedItemsTextView.text =
                    resources.getQuantityString(
                        R.plurals.title_card_selection_toolbar,
                        numberOfSelectedCards,
                        numberOfSelectedCards
                    )
            }
        }
    }

    private fun preventCardItemsJumping() {
        if (!selectionToolbar.isVisible && isSelectionMode) {
            antiJumpingView.isVisible = true
            val gap: Int = appBar.height - 48.dp
            antiJumpingView.updateLayoutParams {
                height = gap
            }
            isAntiJumpingViewActivated = true
        } else if (selectionToolbar.isVisible && !isSelectionMode) {
            antiJumpingView.isVisible = false
            isAntiJumpingViewActivated = false
        }
    }

    private fun updateStatusBarColor(isSelectionMode: Boolean = this.isSelectionMode) {
        if (findNavController().currentDestination?.id == R.id.deck_chooser) return
        if (isSelectionMode) {
            setStatusBarColor(requireActivity(), R.color.selection_toolbar)
        } else {
            setTransparentStatusBar(requireActivity())
        }
    }

    private fun updateAppbarItemsVisibility() {
        setSelectionToolbarVisibility(isVisible = isSelectionMode)
        searchEditText.isEnabled = !isSelectionMode
        searchFrame.isVisible = !isSelectionMode
    }

    private fun setSelectionToolbarVisibility(isVisible: Boolean) {
        if (selectionToolbar.isVisible == isVisible) return
        val transition: Transition = Slide(Gravity.TOP)
        transition.duration = 200
        transition.addTarget(selectionToolbar)
        TransitionManager.beginDelayedTransition(appBar, transition)
        selectionToolbar.isVisible = isVisible
    }

    private fun executeCommand(command: SearchController.Command) {
        when (command) {
            is ShowCardsAreInvertedMessage -> {
                val message = resources.getQuantityString(
                    R.plurals.snackbar_card_selection_action_completed_invert,
                    command.numberOfInvertedCards,
                    command.numberOfInvertedCards
                )
                showCardSelectionActionIsCompletedSnackbar(message)
            }
            is ShowGradeIsChangedMessage -> {
                val message = resources.getQuantityString(
                    R.plurals.snackbar_card_selection_action_completed_change_grade,
                    command.numberOfAffectedCards,
                    command.grade,
                    command.numberOfAffectedCards
                )
                showCardSelectionActionIsCompletedSnackbar(message)
            }
            is ShowCardsAreMarkedAsLearnedMessage -> {
                val message = resources.getQuantityString(
                    R.plurals.snackbar_card_selection_action_completed_mark_as_learned,
                    command.numberOfMarkedCards,
                    command.numberOfMarkedCards
                )
                showCardSelectionActionIsCompletedSnackbar(message)
            }
            is ShowCardsAreMarkedAsUnlearnedMessage -> {
                val message = resources.getQuantityString(
                    R.plurals.snackbar_card_selection_action_completed_mark_as_unlearned,
                    command.numberOfMarkedCards,
                    command.numberOfMarkedCards
                )
                showCardSelectionActionIsCompletedSnackbar(message)
            }
            is ShowCardsAreRemovedMessage -> {
                val message = resources.getQuantityString(
                    R.plurals.snackbar_card_selection_action_completed_remove,
                    command.numberOfRemovedCards,
                    command.numberOfRemovedCards
                )
                showCardSelectionActionIsCompletedSnackbar(message)
            }
            is ShowCardsAreMovedMessage -> {
                val message = resources.getQuantityString(
                    R.plurals.snackbar_card_selection_action_completed_move,
                    command.numberOfMovedCards,
                    command.numberOfMovedCards,
                    command.deckNameToWhichCardsWereMoved
                )
                showCardSelectionActionIsCompletedSnackbar(message)
            }
            is ShowCardsAreCopiedMessage -> {
                val message = resources.getQuantityString(
                    R.plurals.snackbar_card_selection_action_completed_copy,
                    command.numberOfCopiedCards,
                    command.numberOfCopiedCards,
                    command.deckNameToWhichCardsWereCopied
                )
                showCardSelectionActionIsCompletedSnackbar(message)
            }
        }
    }

    private fun showCardSelectionActionIsCompletedSnackbar(message: String) {
        lastShownSnackbar = Snackbar
            .make(
                searchRootView,
                message,
                resources.getInteger(R.integer.duration_deck_is_deleted_snackbar)
            )
            .setAction(
                R.string.snackbar_action_cancel,
                { controller?.dispatch(CancelSnackbarButtonClicked) }
            )
            .apply {
                show()
            }
    }

    override fun onResume() {
        super.onResume()
        appBar.post { appBar.isActivated = cardsRecycler.canScrollVertically(-1) }
        cardsRecycler.addOnScrollListener(scrollListener)
        (activity as MainActivity).registerBackPressInterceptor(backPressInterceptor)
        updatePasteClearButton()
    }

    override fun onPause() {
        super.onPause()
        cardsRecycler.removeOnScrollListener(scrollListener)
        (activity as MainActivity).unregisterBackPressInterceptor(backPressInterceptor)
        searchEditText.hideSoftInput()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cardsRecycler.adapter = null
        updateStatusBarColor(isSelectionMode = false)
        lastShownSnackbar?.dismiss()
        lastShownSnackbar = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            SearchDiScope.close()
        }
    }

    private val backPressInterceptor = MainActivity.BackPressInterceptor {
        when {
            isSelectionMode -> {
                controller?.dispatch(CardSelectionWasCancelled)
                true
            }
            else -> false
        }
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val canScrollUp = recyclerView.canScrollVertically(-1)
            if (appBar.isActivated != canScrollUp) {
                appBar.isActivated = canScrollUp
            }
            if (isAntiJumpingViewActivated) {
                antiJumpingView.updateLayoutParams {
                    height -= abs(dy) / 2
                }
                if (antiJumpingView.height <= 0) {
                    antiJumpingView.isVisible = false
                    isAntiJumpingViewActivated = false
                }
            }
        }
    }
}