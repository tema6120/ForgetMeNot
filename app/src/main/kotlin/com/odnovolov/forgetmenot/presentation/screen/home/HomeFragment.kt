package com.odnovolov.forgetmenot.presentation.screen.home

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import com.google.android.material.snackbar.Snackbar
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.Item
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemAdapter
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemForm.AsCheckBox
import com.odnovolov.forgetmenot.presentation.screen.home.HomeController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.*
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckFragment
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Criterion.*
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Direction.Asc
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Direction.Desc
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSortingBottomSheet
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
    private lateinit var filterDialog: Dialog
    private lateinit var filterAdapter: ItemAdapter
    private var actionMode: ActionMode? = null
    private var resumePauseCoroutineScope: CoroutineScope? = null
    private val fragmentCoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var searchView: SearchView
    private var searchViewText: String? = null
    private var pendingEvent: OutputStreamOpened? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            searchViewText = savedInstanceState.getString(STATE_KEY_SEARCH_VIEW_TEXT)
        }
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
        setHasOptionsMenu(true)
        initFilterDialog()
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    private fun initFilterDialog() {
        filterDialog = ChoiceDialogCreator.create(
            context = requireContext(),
            title = getString(R.string.title_deckpreview_filter_dialog),
            itemForm = AsCheckBox,
            onItemClick = { controller?.dispatch(DisplayOnlyWithTasksCheckboxClicked) },
            takeAdapter = { filterAdapter = it }
        )
        dialogTimeCapsule.register("filterDialog", filterDialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = HomeDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            decksPreviewRecycler.adapter = diScope.deckPreviewAdapter
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
        addCardsButton.setOnClickListener {
            (childFragmentManager.findFragmentByTag("AddDeckFragment") as AddDeckFragment)
                .addDeck()
        }
        filterButton.setOnClickListener {
            filterDialog.show()
        }
        sortingButton.setOnClickListener {
            DeckSortingBottomSheet().show(childFragmentManager, "DeckSortingBottomSheet Tag")
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

                val item = object : Item {
                    override val text = getString(R.string.filter_display_only_with_tasks)
                    override val isSelected = displayOnlyDecksAvailableForExercise
                }
                filterAdapter.submitList(listOf(item))
            }
            deckSorting.observe { deckSorting: DeckSorting ->
                sortingButton.text = getString(
                    when (deckSorting.criterion) {
                        Name -> R.string.sort_by_name
                        CreatedAt -> R.string.sort_by_time_created
                        LastOpenedAt -> R.string.sort_by_time_last_opened
                    }
                )
                val directionIconId: Int = when (deckSorting.direction) {
                    Asc -> R.drawable.ic_arrow_upward
                    Desc -> R.drawable.ic_arrow_downward
                }
                sortingButton.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_sorting, 0, directionIconId, 0
                )
            }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_actions, menu)
        val searchItem = menu.findItem(R.id.action_search)
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(menuItem: MenuItem): Boolean {
                setMenuItemsVisibility(false)
                return true
            }

            override fun onMenuItemActionCollapse(menuItem: MenuItem): Boolean {
                setMenuItemsVisibility(true)
                requireActivity().invalidateOptionsMenu()
                return true
            }

            fun setMenuItemsVisibility(visible: Boolean) {
                for (i in 0 until menu.size()) {
                    val item = menu.getItem(i)
                    if (item !== searchItem)
                        item.isVisible = visible
                }
            }
        })
        searchView = searchItem.actionView as SearchView
        if (!searchViewText.isNullOrEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(searchViewText, false)
            searchView.requestFocus()
        }
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String) = false

            override fun onQueryTextChange(newText: String): Boolean {
                controller?.dispatch(SearchTextChanged(newText))
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_deck -> {
                (childFragmentManager.findFragmentByTag("AddDeckFragment") as AddDeckFragment)
                    .addDeck()
                true
            }
            R.id.action_sort_by -> {
                DeckSortingBottomSheet().show(childFragmentManager, "DeckSortingBottomSheet Tag")
                true
            }
            R.id.action_filter -> {
                filterDialog.show()
                true
            }
            R.id.action_settings -> {
                controller?.dispatch(SettingsButtonClicked)
                true
            }
            R.id.action_help -> {
                controller?.dispatch(HelpButtonClicked)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        resumePauseCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        resumePauseCoroutineScope!!.launch {
            val diScope = HomeDiScope.getAsync() ?: return@launch
            val viewModel = diScope.viewModel
            with(viewModel) {
                decksPreview.observe(
                    resumePauseCoroutineScope!!
                ) { decksPreview: List<DeckPreview> ->
                    diScope.deckPreviewAdapter.submitList(decksPreview)
                    progressBar.visibility = View.GONE
                }
                deckSelectionCount.observe(
                    resumePauseCoroutineScope!!
                ) { deckSelectionCount: DeckSelectionCount ->
                    val (cardsCount, decksCount) = deckSelectionCount
                    actionMode?.title = getString(
                        R.string.deck_selection_action_mode_title,
                        cardsCount,
                        decksCount
                    )
                }
            }
        }
        hideActionBar()
    }

    override fun onPause() {
        super.onPause()
        resumePauseCoroutineScope!!.cancel()
        resumePauseCoroutineScope = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::searchView.isInitialized) {
            outState.putString(STATE_KEY_SEARCH_VIEW_TEXT, searchView.query.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        decksPreviewRecycler.adapter = null
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
        const val STATE_KEY_SEARCH_VIEW_TEXT = "searchViewText"
        const val CREATE_FILE_REQUEST_CODE = 40
    }
}