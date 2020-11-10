package com.odnovolov.forgetmenot.presentation.screen.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.widget.TooltipCompat
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout.Behavior
import com.google.android.material.appbar.AppBarLayout.LayoutParams
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor.paste
import com.odnovolov.forgetmenot.presentation.screen.home.HomeController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.*
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckFragment
import com.odnovolov.forgetmenot.presentation.screen.navhost.NavHostFragment
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_nav_host.*
import kotlinx.coroutines.*

class HomeFragment : BaseFragment() {
    init {
        HomeDiScope.reopenIfClosed()
    }

    private lateinit var viewModel: HomeViewModel
    private var controller: HomeController? = null
    private var actionMode: ActionMode? = null
    private val fragmentCoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var pendingEvent: OutputStreamOpened? = null
    private var tabLayoutMediator: TabLayoutMediator? = null
    private var pagerAdapter: HomePagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentCoroutineScope.launch {
            val diScope = HomeDiScope.getAsync() ?: return@launch
            diScope.viewModel.hasSelectedDecks.observe(
                fragmentCoroutineScope
            ) { hasSelectedDecks: Boolean ->
                if (hasSelectedDecks) {
                    if (actionMode == null) {
                        actionMode = requireActivity().startActionMode(actionModeCallback)
                    }
                } else {
                    actionMode?.finish()
                }
            }
        }
    }

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
        drawerButton.setOnClickListener {
            (parentFragment as NavHostFragment)
                .drawerLayout.openDrawer(GravityCompat.START)
        }
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            setSearchMode(hasFocus)
        }
        searchEditText.observeText { newText: String ->
            controller?.dispatch(SearchTextChanged(newText))
        }
        addCardsButton.setOnClickListener {
            (childFragmentManager.findFragmentByTag("AddDeckFragment") as AddDeckFragment)
                .addDeck()
        }
        setupViewPager()
    }

    private fun setSearchMode(isSearchMode: Boolean) {
        searchFrame.isSelected = isSearchMode
        updateDrawerButton(isSearchMode)
        updateSearchFrameScrollBehavior(isSearchMode)
        setLockModeOfDrawerLayout(isLocked = isSearchMode)
    }

    private fun updateDrawerButton(isSearchMode: Boolean) {
        drawerButton.setImageResource(
            if (isSearchMode)
                R.drawable.ic_arrow_back_colored else
                R.drawable.ic_drawer_colored
        )
        drawerButton.setOnClickListener {
            if (isSearchMode) {
                cancelSearch()
            } else {
                (parentFragment as NavHostFragment)
                    .drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    private fun cancelSearch() {
        searchEditText.hideSoftInput()
        searchEditText.text.clear()
        searchEditText.clearFocus()
    }

    private fun updateSearchFrameScrollBehavior(isSearchMode: Boolean) {
        val params = searchFrame.layoutParams as LayoutParams
        val appBarLayoutParams = appBarLayout.layoutParams as CoordinatorLayout.LayoutParams
        if (isSearchMode) {
            params.scrollFlags = 0
            appBarLayoutParams.behavior = null
            appBarLayout.layoutParams = appBarLayoutParams
        } else {
            params.scrollFlags =
                LayoutParams.SCROLL_FLAG_SCROLL or LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
            appBarLayoutParams.behavior = Behavior()
            appBarLayout.layoutParams = appBarLayoutParams
        }
    }

    private fun setLockModeOfDrawerLayout(isLocked: Boolean) {
        (parentFragment as NavHostFragment).drawerLayout.setDrawerLockMode(
            if (isLocked)
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED else
                DrawerLayout.LOCK_MODE_UNLOCKED
        )
    }

    private fun setupViewPager() {
        homePager.offscreenPageLimit = 1
        pagerAdapter = HomePagerAdapter(this)
        homePager.adapter = pagerAdapter
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
            displayOnlyWithTasks.observe { displayOnlyDecksAvailableForExercise: Boolean ->
                deckListTitleTextView.text = getString(
                    if (displayOnlyDecksAvailableForExercise)
                        R.string.deck_list_title_decks_available_for_exercise else
                        R.string.deck_list_title_all_decks
                )
            }
            hasSearchText.observe { hasSearchText: Boolean ->
                updatePasteButton(hasSearchText)
                updateTabsVisibility(areTabsVisible = hasSearchText)
                updateFoundCardsFragmentVisibility(isFragmentVisible = hasSearchText)
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
            TooltipCompat.setTooltipText(this, contentDescription)
        }
    }

    private fun updateTabsVisibility(areTabsVisible: Boolean) {
        deckListTitleTextView.isVisible = !areTabsVisible
        addCardsButton.isVisible = !areTabsVisible
        searchTabLayout.isVisible = areTabsVisible
    }

    private fun updateFoundCardsFragmentVisibility(isFragmentVisible: Boolean) {
        (homePager.getChildAt(0) as RecyclerView).overScrollMode =
            if (isFragmentVisible)
                View.OVER_SCROLL_IF_CONTENT_SCROLLS else
                View.OVER_SCROLL_NEVER
        pagerAdapter?.isFoundCardsFragmentEnabled = isFragmentVisible
    }

    private fun executeCommand(command: HomeController.Command) {
        when (command) {
            ShowNoCardIsReadyForExerciseMessage -> {
                showToast(R.string.toast_text_no_cards_ready_for_exercise)
            }
            is ShowDeckRemovingMessage -> {
                Snackbar
                    .make(
                        homeFragmentContent,
                        resources.getQuantityString(
                            R.plurals.numberOfDecksRemoved,
                            command.numberOfDecksRemoved,
                            command.numberOfDecksRemoved
                        ),
                        resources.getInteger(R.integer.duration_deck_is_deleted_snackbar)
                    )
                    .setAction(
                        R.string.snackbar_action_cancel,
                        { controller?.dispatch(DecksRemovedSnackbarCancelActionClicked) }
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
            val event = OutputStreamOpened(outputStream)
            if (controller == null) {
                pendingEvent = event
            } else {
                controller!!.dispatch(event)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).registerBackPressInterceptor(backPressInterceptorForCancelSearch)
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity)
            .unregisterBackPressInterceptor(backPressInterceptorForCancelSearch)
    }

    private val backPressInterceptorForCancelSearch = object : MainActivity.BackPressInterceptor {
        override fun onBackPressed(): Boolean {
            return if (searchEditText.hasFocus()) {
                cancelSearch()
                true
            } else {
                false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        homePager.adapter = null
        pagerAdapter = null
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
    }

    override fun onDestroy() {
        super.onDestroy()
        fragmentCoroutineScope.cancel()
        if (needToCloseDiScope()) {
            HomeDiScope.close()
        }
    }

    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.deck_selection_actions, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_start_exercise -> {
                    controller?.dispatch(StartExerciseMenuItemClicked)
                    true
                }
                R.id.action_select_all_decks -> {
                    controller?.dispatch(SelectAllDecksMenuItemClicked)
                    true
                }
                R.id.action_repetition_mode -> {
                    controller?.dispatch(RepetitionModeMultiSelectMenuItemClicked)
                    true
                }
                R.id.action_remove_decks -> {
                    controller?.dispatch(RemoveDecksMenuItemClicked)
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            controller?.dispatch(ActionModeFinished)
            actionMode = null
        }
    }

    companion object {
        const val CREATE_FILE_REQUEST_CODE = 40
    }
}