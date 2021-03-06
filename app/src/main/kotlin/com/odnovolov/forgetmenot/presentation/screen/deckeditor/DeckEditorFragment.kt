package com.odnovolov.forgetmenot.presentation.screen.deckeditor

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.NestedScrollView
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
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorEvent.*
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenTab.Content
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenTab.Settings
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent.DeckContentFragment
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsFragment
import kotlinx.android.synthetic.main.fragment_deck_editor.*
import kotlinx.android.synthetic.main.toolbar_item_selection.*
import kotlinx.coroutines.launch
import kotlin.math.abs

class DeckEditorFragment : BaseFragment() {
    init {
        DeckEditorDiScope.reopenIfClosed()
    }

    private var tabLayoutMediator: TabLayoutMediator? = null
    private var controller: DeckEditorController? = null
    private lateinit var viewModel: DeckEditorViewModel
    private var needTabs = true
    private var isSelectionMode = false
    private var isFullyOnContentPage = false
    private var appbarLayoutOffset: Int = 0
    private var isAntiJumpingViewActivated = false
    private var lastShownSnackbar: Snackbar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_deck_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = DeckEditorDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            observeViewModel()
            controller!!.commands.observe(::executeCommand)
        }
    }

    private fun setupView() {
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        deckNameTextView.setOnClickListener {
            controller?.dispatch(RenameDeckButtonClicked)
        }
        addCardButton.run {
            setOnClickListener { controller?.dispatch(AddCardButtonClicked) }
            setTooltipTextFromContentDescription()
        }
        observeAppbarOffset()
        setupSelectionToolbar()
        setupViewPager()
    }

    private fun setupSelectionToolbar() {
        cancelSelectionButton.run {
            setOnClickListener { controller?.dispatch(CancelledCardSelection) }
            setTooltipTextFromContentDescription()
        }
        selectAllButton.run {
            setOnClickListener { controller?.dispatch(SelectAllCardsButtonClicked) }
            setTooltipTextFromContentDescription()
        }
        removeOptionItem.run {
            setOnClickListener { controller?.dispatch(RemoveCardsCardSelectionOptionSelected) }
            setTooltipTextFromContentDescription()
        }
        moreOptionsButton.run {
            setOnClickListener {
                CardSelectionOptionsBottomSheet()
                    .show(childFragmentManager, "CardSelectionOptionsBottomSheet")
            }
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
        deckEditorViewPager.isUserInputEnabled = false
        deckEditorViewPager.adapter = DeckEditorPagerAdapter(this)
        deckEditorViewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    appBarElevationManager.viewPagerPosition = position
                }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    isFullyOnContentPage = position == 1 && positionOffset == 0f
                    updateAddCardButtonVisibility()
                }
            }
        )
    }

    private fun observeViewModel() {
        with(viewModel) {
            setupViewPager(tabs)
            deckName.observe(deckNameTextView::setText)
            if (needTabs) {
                isSelectionMode.observe { isSelectionMode: Boolean ->
                    this@DeckEditorFragment.isSelectionMode = isSelectionMode
                    preventCardItemsJumping()
                    updateStatusBarColor()
                    updateAppbarItemsVisibility()
                    updateAppbarScrollBehavior()
                    updateViewPagerLocking()
                    updateAddCardButtonVisibility()
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
    }

    private fun setupViewPager(tabs: DeckEditorTabs) {
        val needTabs: Boolean = tabs is DeckEditorTabs.All
        this.needTabs = needTabs
        deckEditorTabLayout.isVisible = needTabs
        deckEditorViewPager.offscreenPageLimit =
            if (needTabs) 1
            else ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
        deckEditorViewPager.isUserInputEnabled = needTabs
        if (isViewFirstCreated) {
            val activeTab: Int = when (tabs) {
                is DeckEditorTabs.All -> {
                    when (tabs.initialTab) {
                        Settings -> 0
                        Content -> 1
                    }
                }
                DeckEditorTabs.OnlyDeckSettings -> 0
            }
            deckEditorViewPager.setCurrentItem(activeTab, false)
        }
        if (needTabs) {
            tabLayoutMediator = TabLayoutMediator(
                deckEditorTabLayout,
                deckEditorViewPager
            ) { tab, position ->
                val customTab = View.inflate(requireContext(), R.layout.tab, null) as TextView
                customTab.text = getString(
                    when (position) {
                        0 -> R.string.tab_name_settings
                        1 -> R.string.tab_name_content
                        else -> throw IllegalArgumentException("position must be in 0..1")
                    }
                )
                tab.customView = customTab
            }.apply { attach() }
        }
    }

    private fun preventCardItemsJumping() {
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

    private fun updateStatusBarColor(isSelectionMode: Boolean = this.isSelectionMode) {
        if (findNavController().currentDestination?.id == R.id.deck_chooser) return
        if (isSelectionMode) {
            setStatusBarColor(requireActivity(), R.color.colorAccent)
        } else {
            setTransparentStatusBar(requireActivity())
        }
    }

    private fun updateAppbarItemsVisibility() {
        setSelectionToolbarVisibility(isVisible = isSelectionMode)
        backButton.isVisible = !isSelectionMode
        deckNameTextView.isVisible = !isSelectionMode
        deckEditorTabLayout.isVisible = !isSelectionMode
        appBarLayout.requestLayout()
    }

    private fun setSelectionToolbarVisibility(isVisible: Boolean) {
        if (selectionToolbar.isVisible == isVisible) return
        val transition: Transition = Slide(Gravity.TOP)
        transition.duration = 200
        transition.addTarget(selectionToolbar)
        TransitionManager.beginDelayedTransition(appBarLayout, transition)
        selectionToolbar.isVisible = isVisible
    }

    private fun updateAppbarScrollBehavior() {
        val scrollFlags =
            if (isSelectionMode) {
                0
            } else {
                AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
                        AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
            }
        (backButton.layoutParams as AppBarLayout.LayoutParams).scrollFlags = scrollFlags
        (deckNameTextView.layoutParams as AppBarLayout.LayoutParams).scrollFlags = scrollFlags
        appBarLayout.requestLayout()
    }

    private fun updateViewPagerLocking() {
        deckEditorViewPager.isUserInputEnabled = !isSelectionMode
        if (isSelectionMode) {
            deckEditorViewPager.setCurrentItem(1, true)
        }
    }

    private fun updateAddCardButtonVisibility() {
        val shouldBeVisible: Boolean = !isSelectionMode && isFullyOnContentPage
        with(addCardButton) {
            if (isVisible != shouldBeVisible) {
                if (shouldBeVisible) show() else hide()
            }
        }
    }

    private fun executeCommand(command: DeckEditorController.Command) {
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
                coordinatorLayout,
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

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        when (childFragment) {
            is DeckSettingsFragment -> {
                childFragment.scrollListener =
                    NestedScrollView.OnScrollChangeListener { nestedScrollView, _, _, _, _ ->
                        appBarElevationManager.canDeckSettingsScrollUp =
                            nestedScrollView?.canScrollVertically(-1) ?: false
                    }
            }
            is DeckContentFragment -> {
                childFragment.scrollListener = object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        appBarElevationManager.canDeckContentScrollUp =
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
        (activity as MainActivity).registerBackPressInterceptor(backPressInterceptor)
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).unregisterBackPressInterceptor(backPressInterceptor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
        deckEditorViewPager.adapter = null
        lastShownSnackbar?.dismiss()
        lastShownSnackbar = null
        updateStatusBarColor(isSelectionMode = false)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            DeckEditorDiScope.close()
        }
    }

    private val backPressInterceptor = MainActivity.BackPressInterceptor {
        when {
            isSelectionMode -> {
                controller?.dispatch(CancelledCardSelection)
                true
            }
            else -> false
        }
    }

    private val appBarElevationManager = object {
        var viewPagerPosition = 0
            set(value) {
                field = value
                updateAppBarElevation()
            }

        var canDeckSettingsScrollUp = false
            set(value) {
                field = value
                updateAppBarElevation()
            }

        var canDeckContentScrollUp = false
            set(value) {
                field = value
                updateAppBarElevation()
            }

        private fun updateAppBarElevation() {
            val shouldBeElevated = viewPagerPosition == 0 && canDeckSettingsScrollUp ||
                    viewPagerPosition == 1 && canDeckContentScrollUp
            if (appBarLayout.isActivated != shouldBeElevated) {
                appBarLayout.isActivated = shouldBeElevated
            }
        }
    }
}