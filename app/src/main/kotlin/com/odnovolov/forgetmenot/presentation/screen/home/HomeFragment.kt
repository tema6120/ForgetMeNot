package com.odnovolov.forgetmenot.presentation.screen.home

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor.paste
import com.odnovolov.forgetmenot.presentation.screen.home.HomeController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.*
import com.odnovolov.forgetmenot.presentation.screen.navhost.NavHostFragment
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_nav_host.*
import kotlinx.android.synthetic.main.toolbar_item_selection.*
import kotlinx.coroutines.launch
import kotlin.math.abs

class HomeFragment : BaseFragment() {
    init {
        HomeDiScope.reopenIfClosed()
    }

    private lateinit var viewModel: HomeViewModel
    private var controller: HomeController? = null
    private var tabLayoutMediator: TabLayoutMediator? = null
    private var appbarLayoutOffset: Int = 0
    private var backPressInterceptor: MainActivity.BackPressInterceptor? = null
    private var isAntiJumpingViewActivated = false
    private var lastShownSnackbar: Snackbar? = null
    private var selectionMode = SelectionMode.Off
    private val isSelectionMode: Boolean get() = selectionMode != SelectionMode.Off

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = HomeDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            observeViewModel()
            controller!!.commands.observe(onEach = ::executeCommand)
        }
    }

    private fun setupView() {
        setupSearchFrame()
        setupSelectionToolbar()
        observeAppbarOffset()
        setupViewPager()
        setupBottomButtons()
    }

    private fun setupSearchFrame() {
        drawerButton.setOnClickListener {
            openDrawer()
        }
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            searchFrame.isSelected = hasFocus
            updateDrawerButton()
            updateDrawerLayoutLockMode()
            updateSearchFrameScrollFlags()
            updateAppbarScrollBehavior()
            if (!hasFocus) {
                searchEditText.hideSoftInput()
            }
        }
        var needToSkipFirstText = !isViewFirstCreated
        searchEditText.observeText { newText: String ->
            if (needToSkipFirstText) {
                needToSkipFirstText = false
            } else {
                controller?.dispatch(SearchTextChanged(newText))
            }
        }
    }

    private fun setupSelectionToolbar() {
        cancelSelectionButton.run {
            setOnClickListener { controller?.dispatch(CancelledSelection) }
            setTooltipTextFromContentDescription()
        }
        selectAllButton.run {
            setOnClickListener { controller?.dispatch(SelectAllSelectionToolbarButtonClicked) }
            setTooltipTextFromContentDescription()
        }
        removeOptionItem.run {
            setOnClickListener { controller?.dispatch(RemoveSelectionToolbarButtonClicked) }
            setTooltipTextFromContentDescription()
        }
        moreOptionsButton.run {
            setOnClickListener { controller?.dispatch(MoreSelectionToolbarButtonClicked) }
            setTooltipTextFromContentDescription()
        }
    }

    private fun observeAppbarOffset() {
        appBarLayout.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
                appbarLayoutOffset = verticalOffset
            }
        )
    }

    private fun setupViewPager() {
        homePager.offscreenPageLimit = 1
        homePager.adapter = HomePagerAdapter(this)
        tabLayoutMediator = TabLayoutMediator(
            searchTabLayout,
            homePager
        ) { tab, position ->
            val customTab = View.inflate(requireContext(), R.layout.tab, null) as TextView
            customTab.text = getString(
                when (position) {
                    0 -> R.string.tab_decks
                    1 -> R.string.tab_cards
                    else -> throw IllegalArgumentException("position must be in 0..1")
                }
            )
            tab.customView = customTab
        }.apply {
            attach()
        }
        homePager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                appBarElevationManager.viewPagerPosition = position
            }
        })
    }

    private fun setupBottomButtons() {
        bottomButtonsRow.layoutTransition.disableTransitionType(LayoutTransition.APPEARING)
        autoplayButton.setOnClickListener {
            controller?.dispatch(AutoplayButtonClicked)
        }
        exerciseButton.setOnClickListener {
            controller?.dispatch(ExerciseButtonClicked)
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            hasSearchText.observe { hasSearchText: Boolean ->
                deckListTitleTextView.isVisible = !hasSearchText
                addCardsFragment.isVisible = !hasSearchText
                searchTabLayout.isVisible = hasSearchText
                updatePasteButton(hasSearchText)
                updateViewPagerLocking()
            }
            selectionMode.observe { selectionMode: SelectionMode ->
                this@HomeFragment.selectionMode = selectionMode
                preventSelectedItemsFromJumping()
                updateStatusBarColor()
                updateAppbarItemsVisibility()
                updateSearchFrameScrollFlags()
                updateDrawerLayoutLockMode()
                updateViewPagerLocking()
            }
            selectionToolbarTitle.observe(::updateSelectionToolbarTitle)
            deckListTitle.observe { deckListTitle: DeckListTitle ->
                deckListTitleTextView.text =
                    if (deckListTitle.deckListName == null) {
                        getString(
                            if (deckListTitle.onlyDecksAvailableForExercise)
                                R.string.deck_list_title_decks_available_for_exercise else
                                R.string.deck_list_title_all_decks
                        )
                    } else {
                        if (deckListTitle.onlyDecksAvailableForExercise) {
                            getString(
                                R.string.deck_list_title_specific_deck_list_available_for_exercise,
                                deckListTitle.deckListName
                            )
                        } else {
                            "'${deckListTitle.deckListName}'"
                        }
                    }
            }
            isAutoplayButtonVisible.observe { isVisible: Boolean ->
                autoplayButton.isVisible = isVisible
                updateExerciseButtonMargin()
            }
            isExerciseButtonVisible.observe { isVisible: Boolean ->
                exerciseButton.isVisible = isVisible
                updateExerciseButtonMargin()
            }
            numberOfSelectedCardsAvailableForExercise.observe { cardsCount: Int? ->
                exerciseButton.text =
                    if (cardsCount == null)
                        getString(R.string.text_exercise_button) else
                        getString(R.string.text_exercise_button_with_cards_count, cardsCount)
            }
            searchResultFromOnlyCards.observe {
                if (homePager.currentItem == 0 && homePager.isUserInputEnabled) {
                    homePager.setCurrentItem(1, true)
                }
            }
            areFilesBeingReading.observe { areFilesBeingReading: Boolean ->
                progressBarFrame.isVisible = areFilesBeingReading
            }
            areCardsBeingSearched.observe { isSearching: Boolean ->
                searchProgressBar.isInvisible = !isSearching
            }
        }
    }

    private fun updatePasteButton(hasSearchText: Boolean) {
        with(pasteButton) {
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
            setTooltipTextFromContentDescription()
        }
    }

    private fun updateViewPagerLocking() {
        val isLocked: Boolean = isSelectionMode || !searchTabLayout.isVisible
        homePager.isUserInputEnabled = !isLocked
        if (isLocked) {
            val currentViewPagerItem = if (selectionMode == SelectionMode.CardSelection) 1 else 0
            homePager.setCurrentItem(currentViewPagerItem, true)
        }
    }

    private fun preventSelectedItemsFromJumping() {
        if (!selectionToolbar.isVisible && isSelectionMode) {
            antiJumpingView.isVisible = true
            val appBarRealHeight: Int = appBarLayout.height + appbarLayoutOffset
            val gap: Int = appBarRealHeight - 48.dp
            antiJumpingView.updateLayoutParams {
                height = gap
            }
            isAntiJumpingViewActivated = true
        } else if (selectionToolbar.isVisible && !isSelectionMode) {
            antiJumpingView.isVisible = false
            isAntiJumpingViewActivated = false
        }
    }

    private fun updateStatusBarColor(isColorful: Boolean = isSelectionMode) {
        if (findNavController().currentDestination?.id == R.id.deck_chooser) return
        if (isColorful) {
            setStatusBarColor(requireActivity(), R.color.colorAccent)
        } else {
            setTransparentStatusBar(requireActivity())
        }
    }

    private fun updateAppbarItemsVisibility() {
        updateSelectionToolbarVisibility()
        searchEditText.isEnabled = !isSelectionMode
        searchFrame.isVisible = !isSelectionMode
        headline.isVisible = !isSelectionMode
        if (searchFrame.isVisible && searchEditText.text.isNotEmpty()) {
            searchEditText.requestFocus()
        }
    }

    private fun updateSelectionToolbarVisibility() {
        if (selectionToolbar.isVisible == isSelectionMode) return
        val transition: Transition = Slide(Gravity.TOP)
        transition.duration = 200
        transition.addTarget(selectionToolbar)
        TransitionManager.beginDelayedTransition(appBarLayout, transition)
        selectionToolbar.isVisible = isSelectionMode
    }

    private fun updateSearchFrameScrollFlags() {
        val searchFrameLayoutParams = searchFrame.layoutParams as AppBarLayout.LayoutParams
        searchFrameLayoutParams.scrollFlags =
            if (searchEditText.hasFocus() || isSelectionMode) {
                0
            } else {
                AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
                        AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
            }
    }

    private fun updateDrawerLayoutLockMode() {
        val isLocked: Boolean = searchEditText.hasFocus() || isSelectionMode
        (parentFragment as NavHostFragment).drawerLayout.setDrawerLockMode(
            if (isLocked)
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED else
                DrawerLayout.LOCK_MODE_UNLOCKED
        )
    }

    private fun updateSelectionToolbarTitle(selectionToolbarTitle: SelectionToolbarTitle?) {
        if (selectionToolbarTitle == null) return
        numberOfSelectedItemsTextView.text = when (selectionToolbarTitle) {
            is SelectionToolbarTitle.NumberOfSelectedDecks -> {
                resources.getQuantityString(
                    R.plurals.title_selection_toolbar_number_of_selected_decks,
                    selectionToolbarTitle.numberOfSelectedDecks,
                    selectionToolbarTitle.numberOfSelectedDecks
                )
            }
            SelectionToolbarTitle.ChooseDecksToPlay -> {
                getString(R.string.title_deck_selection_toolbar_choose_decks_to_play)
            }
            SelectionToolbarTitle.ChooseDecksForExercise -> {
                getString(R.string.title_deck_selection_toolbar_choose_decks_for_exercise)
            }
            is SelectionToolbarTitle.NumberOfSelectedCards -> {
                resources.getQuantityString(
                    R.plurals.title_card_selection_toolbar,
                    selectionToolbarTitle.numberOfSelectedCards,
                    selectionToolbarTitle.numberOfSelectedCards
                )
            }
        }
        val areSelectionButtonsVisible: Boolean =
            selectionToolbarTitle != SelectionToolbarTitle.ChooseDecksToPlay
                    && selectionToolbarTitle != SelectionToolbarTitle.ChooseDecksForExercise
        removeOptionItem.isVisible = areSelectionButtonsVisible
        moreOptionsButton.isVisible = areSelectionButtonsVisible
    }

    private fun updateExerciseButtonMargin() {
        exerciseButton.updateLayoutParams<ConstraintLayout.LayoutParams> {
            marginStart = if (autoplayButton.isVisible) 0 else 20.dp
        }
    }

    private fun updateAppbarScrollBehavior() {
        val appBarLayoutParams = appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
        appBarLayoutParams.behavior =
            if (searchEditText.hasFocus()) {
                null
            } else {
                AppBarLayout.Behavior()
            }
        appBarLayout.requestLayout()
    }

    private fun updateDrawerButton() {
        val isSearchMode: Boolean = searchEditText.hasFocus()
        with(drawerButton) {
            setImageResource(
                if (isSearchMode)
                    R.drawable.ic_round_keyboard_backspace_24_colored else
                    R.drawable.ic_drawer_colored
            )
            setOnClickListener {
                if (isSearchMode) {
                    cancelSearch()
                } else {
                    openDrawer()
                }
            }
            contentDescription = getString(
                if (isSearchMode)
                    R.string.description_back_button2 else
                    R.string.description_drawer_button
            )
            setTooltipTextFromContentDescription()
        }
    }

    private fun cancelSearch() {
        searchEditText.text.clear()
        searchEditText.clearFocus()
    }

    private fun openDrawer() {
        (parentFragment as NavHostFragment)
            .drawerLayout.openDrawer(GravityCompat.START)
    }

    private fun executeCommand(command: HomeController.Command) {
        when (command) {
            ShowNoCardIsReadyForExerciseMessage -> {
                showToast(R.string.toast_text_no_cards_ready_for_exercise)
            }
            ShowDeckOptions -> {
                DeckOptionsBottomSheet().show(childFragmentManager, "DeckOptionsBottomSheet")
            }
            ShowDeckSelectionOptions -> {
                DeckSelectionOptionsBottomSheet()
                    .show(childFragmentManager, "DeckSelectionOptionsBottomSheet")
            }
            ShowCardSelectionOptions -> {
                CardSelectionOptionsBottomSheet()
                    .show(childFragmentManager, "CardSelectionOptionsBottomSheet")
            }
            is ShowDeckRemovingMessage -> {
                lastShownSnackbar = Snackbar
                    .make(
                        homeRootView,
                        resources.getQuantityString(
                            R.plurals.toast_decks_removing,
                            command.numberOfRemovedDecks,
                            command.numberOfRemovedDecks
                        ),
                        resources.getInteger(R.integer.duration_deck_is_deleted_snackbar)
                    )
                    .setAction(
                        R.string.snackbar_action_cancel,
                        { controller?.dispatch(RemovedDecksSnackbarCancelButtonClicked) }
                    ).apply {
                        show()
                    }
            }
            is ShowDeckMergingMessage -> {
                lastShownSnackbar = Snackbar
                    .make(
                        homeRootView,
                        resources.getQuantityString(
                            R.plurals.toast_decks_merging,
                            command.numberOfMergedDecks,
                            command.numberOfMergedDecks,
                            command.deckNameMergedInto
                        ),
                        resources.getInteger(R.integer.duration_deck_is_deleted_snackbar)
                    )
                    .setAction(
                        R.string.snackbar_action_cancel,
                        { controller?.dispatch(MergedDecksSnackbarCancelButtonClicked) }
                    ).apply {
                        show()
                    }
            }
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
                homeRootView,
                message,
                resources.getInteger(R.integer.duration_deck_is_deleted_snackbar)
            )
            .setAction(
                R.string.snackbar_action_cancel,
                { controller?.dispatch(CancelCardSelectionActionSnackbarButtonClicked) }
            )
            .apply {
                show()
            }
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        when (childFragment) {
            is DeckListFragment -> {
                childFragment.scrollListener = object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        appBarElevationManager.canDeckListScrollUp =
                            recyclerView.canScrollVertically(-1)
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
            is FoundCardsFragment -> {
                childFragment.scrollListener = object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        appBarElevationManager.canCardListScrollUp =
                            recyclerView.canScrollVertically(-1)
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
        }
    }

    override fun onResume() {
        super.onResume()
        backPressInterceptor = MainActivity.BackPressInterceptor {
            when {
                selectionToolbar.isVisible -> {
                    controller?.dispatch(CancelledSelection)
                    true
                }
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
        if (searchEditText.text.isNotEmpty()) {
            searchEditText.post {
                searchEditText.requestFocus()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).unregisterBackPressInterceptor(backPressInterceptor!!)
        backPressInterceptor = null
        searchEditText.hideSoftInput()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        homePager.adapter = null
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
        lastShownSnackbar?.dismiss()
        lastShownSnackbar = null
        updateStatusBarColor(isColorful = false)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            HomeDiScope.close()
        }
    }

    private val appBarElevationManager = object {
        var viewPagerPosition = 0
            set(value) {
                field = value
                updateAppBarElevation()
            }

        var canDeckListScrollUp = false
            set(value) {
                field = value
                updateAppBarElevation()
            }

        var canCardListScrollUp = false
            set(value) {
                field = value
                updateAppBarElevation()
            }

        private fun updateAppBarElevation() {
            val shouldBeElevated = viewPagerPosition == 0 && canDeckListScrollUp ||
                    viewPagerPosition == 1 && canCardListScrollUp
            if (appBarLayout.isActivated != shouldBeElevated) {
                appBarLayout.isActivated = shouldBeElevated
            }
        }
    }
}