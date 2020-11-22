package com.odnovolov.forgetmenot.presentation.screen.home

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.TooltipCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.searcher.SearchCard
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor.paste
import com.odnovolov.forgetmenot.presentation.screen.home.DeckListItem.DeckPreview
import com.odnovolov.forgetmenot.presentation.screen.home.HomeController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.*
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckFragment
import com.odnovolov.forgetmenot.presentation.screen.navhost.NavHostFragment
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_nav_host.*
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.math.abs

class HomeFragment : BaseFragment() {
    init {
        HomeDiScope.reopenIfClosed()
    }

    private lateinit var viewModel: HomeViewModel
    private var controller: HomeController? = null
    private var pendingEvent: FileForExportDeckIsReady? = null
    private var tabLayoutMediator: TabLayoutMediator? = null
    private var isSearchingAfterPasteButtonClicked: Boolean = false
    private var appbarLayoutOffset: Int = 0

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
            pendingEvent?.let(controller!!::dispatch)
            pendingEvent = null
        }
    }

    private fun setupView() {
        setupSearchFrame()
        setupAddCardsButton()
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
        searchEditText.observeText { newText: String ->
            controller?.dispatch(SearchTextChanged(newText))
            isSearchingAfterPasteButtonClicked = false
        }
    }

    private fun setupAddCardsButton() {
        addCardsButton.setOnClickListener {
            (childFragmentManager.findFragmentByTag("AddDeckFragment") as AddDeckFragment)
                .showAddCardsPopup(anchor = addCardsButton)
        }
    }

    private fun setupBottomButtons() {
        autoplayButton.setOnClickListener {
            controller?.dispatch(AutoplayButtonClicked)
        }
        exerciseButton.setOnClickListener {
            controller?.dispatch(ExerciseButtonClicked)
        }
    }

    private fun setupSelectionToolbar() {
        cancelSelectionButton.setOnClickListener {
            controller?.dispatch(SelectionCancelled)
        }
        selectAllButton.setOnClickListener {
            controller?.dispatch(SelectAllDecksButtonClicked)
        }
        removeDecksButton.setOnClickListener {
            controller?.dispatch(RemoveDecksButtonClicked)
        }
    }

    private fun observeAppbarOffset() {
        appBarLayout.addOnOffsetChangedListener(
            OnOffsetChangedListener { _, verticalOffset -> appbarLayoutOffset = verticalOffset }
        )
    }

    private fun setupViewPager() {
        homePager.offscreenPageLimit = 1
        homePager.adapter = HomePagerAdapter(this)
        tabLayoutMediator = TabLayoutMediator(
            searchTabLayout,
            homePager
        ) { tab, position ->
            val customTab: TextView =
                View.inflate(requireContext(), R.layout.tab, null) as TextView
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
    }

    private fun observeViewModel() {
        with(viewModel) {
            hasSearchText.observe { hasSearchText: Boolean ->
                deckListTitleTextView.isVisible = !hasSearchText
                addCardsButton.isVisible = !hasSearchText
                searchTabLayout.isVisible = hasSearchText
                updatePasteButton(hasSearchText)
                updateViewPagerLocking()
            }
            deckSelection.observe { deckSelection: DeckSelection? ->
                preventDeckItemsJumping(deckSelection)
                selectionToolbar.isVisible = deckSelection != null
                updateSearchFrameScrollFlags()
                searchFrame.isVisible = deckSelection == null
                headline.isVisible = deckSelection == null
                if (searchFrame.isVisible && searchEditText.text.isNotEmpty()) {
                    searchEditText.requestFocus()
                }
                updateExerciseButtonLayoutParams(deckSelection)
                deckSelection?.let { selection: DeckSelection ->
                    numberOfSelectedDecksTextView.text = resources.getQuantityString(
                        R.plurals.number_of_selected_decks,
                        selection.selectedDeckIds.size,
                        selection.selectedDeckIds.size
                    )
                }
                removeDecksButton.isVisible = deckSelection != null
                        && deckSelection.purpose == DeckSelection.Purpose.General
                updateStatusBarColor(deckSelection != null)
                updateDrawerLayoutLockMode()
                updateViewPagerLocking()
            }
            displayOnlyDecksAvailableForExercise.observe { displayOnlyDecksAvailableForExercise: Boolean ->
                deckListTitleTextView.text = getString(
                    if (displayOnlyDecksAvailableForExercise)
                        R.string.deck_list_title_decks_available_for_exercise else
                        R.string.deck_list_title_all_decks
                )
            }
            autoplayButtonState.observe { buttonState: ButtonState ->
                autoplayButton.isVisible = buttonState != ButtonState.Invisible
                autoplayButton.isEnabled = buttonState != ButtonState.Inactive
                autoplayButton.text =
                    getString(
                        if (buttonState == ButtonState.Inactive)
                            R.string.text_autoplay_button_unable else
                            R.string.text_autoplay_button
                    )
                autoplayButton.icon =
                    if (buttonState == ButtonState.Inactive) {
                        null
                    } else {
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_play)
                    }
            }
            exerciseButtonState.observe { buttonState: ButtonState ->
                exerciseButton.isVisible = buttonState != ButtonState.Invisible
                exerciseButton.isEnabled = buttonState != ButtonState.Inactive
            }
            combine(
                exerciseButtonState,
                numberOfSelectedCardsAvailableForExercise
            ) { buttonState: ButtonState, cardsCount: Int? ->
                exerciseButton.text =
                    when {
                        buttonState == ButtonState.Inactive ->
                            getString(R.string.text_exercise_button_unable)
                        cardsCount != null ->
                            getString(
                                R.string.text_exercise_button_with_cards_count,
                                cardsCount
                            )
                        else ->
                            getString(R.string.text_exercise_button)
                    }
            }.observe()
            combine(decksPreview, foundCards) { foundDecks: List<DeckPreview>,
                                                foundCards: List<SearchCard>
                ->
                if (isSearchingAfterPasteButtonClicked
                    && foundDecks.isEmpty()
                    && foundCards.isNotEmpty()
                    && homePager.isUserInputEnabled
                ) {
                    homePager.setCurrentItem(1, true)
                }
            }.observe()
        }
    }

    private fun preventDeckItemsJumping(deckSelection: DeckSelection?) {
        if (!selectionToolbar.isVisible && deckSelection != null) {
            antiJumpingView.isVisible = true
            val appBarRealHeight: Int = appBarLayout.height + appbarLayoutOffset
            val gap = appBarRealHeight - 48.dp
            antiJumpingView.updateLayoutParams {
                height = gap
            }
            val deckListFragment =
                childFragmentManager.findFragmentByTag("f0") as DeckListFragment
            deckListFragment.scrollListener = { dy: Int ->
                antiJumpingView.updateLayoutParams {
                    height -= abs(dy)
                }
                if (antiJumpingView.height <= 0) {
                    antiJumpingView.isVisible = false
                    deckListFragment.scrollListener = null
                }
            }
        } else if (selectionToolbar.isVisible && deckSelection == null) {
            antiJumpingView.isVisible = false
            val deckListFragment =
                childFragmentManager.findFragmentByTag("f0") as DeckListFragment
            deckListFragment.scrollListener = null
        }
    }

    private fun updateExerciseButtonLayoutParams(deckSelection: DeckSelection?) {
        val marginStart: Int =
            if (deckSelection != null
                && deckSelection.purpose == DeckSelection.Purpose.ForExercise
            ) {
                20.dp
            } else {
                0
            }
        val layoutParams = exerciseButton.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.updateMarginsRelative(start = marginStart)
        exerciseButton.layoutParams = layoutParams
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
                    isSearchingAfterPasteButtonClicked = true
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

    private fun updateViewPagerLocking() {
        val isLocked: Boolean = selectionToolbar.isVisible || !searchTabLayout.isVisible
        homePager.isUserInputEnabled = !isLocked
        if (isLocked) {
            homePager.setCurrentItem(0, true)
        }
    }

    private fun executeCommand(command: HomeController.Command) {
        when (command) {
            ShowNoCardIsReadyForExerciseMessage -> {
                showToast(R.string.toast_text_no_cards_ready_for_exercise)
            }
            is ShowDeckRemovingMessage -> {
                Snackbar
                    .make(
                        homeRootView,
                        resources.getQuantityString(
                            R.plurals.numberOfDecksRemoved,
                            command.numberOfDecksRemoved,
                            command.numberOfDecksRemoved
                        ),
                        resources.getInteger(R.integer.duration_deck_is_deleted_snackbar)
                    )
                    .setAction(
                        R.string.snackbar_action_cancel,
                        { controller?.dispatch(DecksRemovedSnackbarCancelButtonClicked) }
                    )
                    .show()
            }
            is ShowCreateFileDialog -> {
                showCreateFileDialog(command.fileName)
            }
            ShowDeckIsExportedMessage -> {
                showToast(R.string.toast_deck_is_exported)
            }
            is ShowExportErrorMessage -> {
                val errorMessage = getString(
                    R.string.toast_error_while_exporting_deck,
                    command.e.message
                )
                showToast(errorMessage)
            }
        }
    }

    private fun showCreateFileDialog(fileName: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TITLE, fileName)
        startActivityForResult(intent, CREATE_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode != CREATE_FILE_REQUEST_CODE
            || resultCode != Activity.RESULT_OK
            || intent == null
        ) {
            return
        }
        val uri = intent.data ?: return
        val outputStream = requireContext().contentResolver?.openOutputStream(uri)
        if (outputStream != null) {
            val event = FileForExportDeckIsReady(outputStream)
            if (controller == null) {
                pendingEvent = event
            } else {
                controller!!.dispatch(event)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).registerBackPressInterceptor(backPressInterceptor)
        if (searchEditText.text.isNotEmpty()) {
            searchEditText.post {
                searchEditText.requestFocus()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).unregisterBackPressInterceptor(backPressInterceptor)
        searchEditText.hideSoftInput()
    }

    private val backPressInterceptor = object : MainActivity.BackPressInterceptor {
        override fun onBackPressed(): Boolean {
            return when {
                selectionToolbar.isVisible -> {
                    controller?.dispatch(SelectionCancelled)
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
    }

    private fun cancelSearch() {
        searchEditText.hideSoftInput()
        searchEditText.text.clear()
        searchEditText.clearFocus()
    }

    private fun updateDrawerLayoutLockMode() {
        val isLocked: Boolean = searchEditText.hasFocus() || selectionToolbar.isVisible
        (parentFragment as NavHostFragment).drawerLayout.setDrawerLockMode(
            if (isLocked)
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED else
                DrawerLayout.LOCK_MODE_UNLOCKED
        )
    }

    private fun updateSearchFrameScrollFlags() {
        val searchFrameLayoutParams = searchFrame.layoutParams as AppBarLayout.LayoutParams
        searchFrameLayoutParams.scrollFlags =
            if (searchEditText.hasFocus() || selectionToolbar.isVisible) {
                0
            } else {
                AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or
                        AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
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
    }

    private fun updateStatusBarColor(isSelectionMode: Boolean) {
        requireActivity().window.statusBarColor =
            if (isSelectionMode) {
                ContextCompat.getColor(requireContext(), R.color.colorAccent)
            } else {
                Color.TRANSPARENT
            }
        if (VERSION.SDK_INT >= VERSION_CODES.M) {
            val visibility = if (isSelectionMode) {
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            } else {
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            requireActivity().window.decorView.systemUiVisibility = visibility
        } else {
            if (isSelectionMode) {
                requireActivity().window.clearFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                )
            } else {
                requireActivity().window.addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                )
            }
        }
    }

    private fun updateDrawerButton() {
        val isSearchMode: Boolean = searchEditText.hasFocus()
        with(drawerButton) {
            setImageResource(
                if (isSearchMode)
                    R.drawable.ic_arrow_back_colored else
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
            TooltipCompat.setTooltipText(this, contentDescription)
        }
    }

    private fun openDrawer() {
        (parentFragment as NavHostFragment)
            .drawerLayout.openDrawer(GravityCompat.START)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        homePager.adapter = null
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            HomeDiScope.close()
        }
    }

    companion object {
        const val CREATE_FILE_REQUEST_CODE = 40
    }
}