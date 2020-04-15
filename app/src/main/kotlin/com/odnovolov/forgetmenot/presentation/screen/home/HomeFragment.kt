package com.odnovolov.forgetmenot.presentation.screen.home

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.Item
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemAdapter
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemForm.AsCheckBox
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.screen.home.HomeCommand.ShowDeckRemovingMessage
import com.odnovolov.forgetmenot.presentation.screen.home.HomeCommand.ShowNoCardIsReadyForExerciseMessage
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.*
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckFragment
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSortingBottomSheet
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class HomeFragment : BaseFragment() {
    init {
        HomeDiScope.reopenIfClosed()
    }

    private lateinit var viewModel: HomeViewModel
    private var controller: HomeController? = null
    private lateinit var deckPreviewAdapter: DeckPreviewAdapter
    private lateinit var filterDialog: Dialog
    private lateinit var filterAdapter: ItemAdapter<Item>
    private var actionMode: ActionMode? = null
    private var resumePauseCoroutineScope: CoroutineScope? = null
    private lateinit var searchView: SearchView
    private var searchViewText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            searchViewText = savedInstanceState.getString(STATE_KEY_SEARCH_VIEW_TEXT)
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
        filterDialog = ChoiceDialogCreator.create<Item>(
            context = requireContext(),
            title = getString(R.string.title_deckpreview_filter_dialog),
            itemForm = AsCheckBox,
            onItemClick = { controller?.dispatch(DisplayOnlyWithTasksCheckboxClicked) },
            takeAdapter = { filterAdapter = it }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewCoroutineScope!!.launch() {
            val diScope = HomeDiScope.get()
            controller = diScope.controller
            viewModel = diScope.viewModel
            deckPreviewAdapter = diScope.deckPreviewAdapter
            decksPreviewRecycler.adapter = deckPreviewAdapter
            observeViewModel()
            controller!!.commands.observe(onEach = ::executeCommand)
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            displayOnlyWithTasks.observe { displayOnlyWithTasks: Boolean ->
                val item = object : Item {
                    override val text = getString(R.string.filter_display_only_with_tasks)
                    override val isSelected = displayOnlyWithTasks
                }
                filterAdapter.items = listOf(item)
            }
            deckSelectionCount.observe { deckSelectionCount: DeckSelectionCount? ->
                if (deckSelectionCount == null) {
                    actionMode?.finish()
                } else {
                    if (actionMode == null) {
                        actionMode = requireActivity().startActionMode(actionModeCallback)
                    }
                    val (cardsCount, decksCount) = deckSelectionCount
                    actionMode!!.title =
                        getString(R.string.deck_selection_action_mode_title, cardsCount, decksCount)
                }
            }
        }
    }

    private fun executeCommand(command: HomeCommand) {
        when (command) {
            ShowNoCardIsReadyForExerciseMessage -> {
                Toast.makeText(
                    requireContext(),
                    R.string.toast_text_no_cards_ready_for_exercise,
                    Toast.LENGTH_SHORT
                ).show()
            }
            is ShowDeckRemovingMessage -> {
                Snackbar
                    .make(
                        homeFragmentRootView,
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
                val addDeckFragment = childFragmentManager
                    .findFragmentById(R.id.addDeckFragment) as AddDeckFragment
                addDeckFragment.showFileChooser()
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        resumePauseCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        resumePauseCoroutineScope!!.launch {
            val viewModel = HomeDiScope.get().viewModel
            viewModel.decksPreview.collect {
                if (isActive) {
                    deckPreviewAdapter.submitList(it)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        resumePauseCoroutineScope!!.cancel()
        resumePauseCoroutineScope = null
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState
            ?.getBundle(STATE_KEY_FILTER_DIALOG)
            ?.let(filterDialog::onRestoreInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::filterDialog.isInitialized) {
            outState.putBundle(STATE_KEY_FILTER_DIALOG, filterDialog.onSaveInstanceState())
        }
        if (::searchView.isInitialized) {
            outState.putString(STATE_KEY_SEARCH_VIEW_TEXT, searchView.query.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        decksPreviewRecycler.adapter = null
        // sometimes deckSelectionCount.observe() doesn't keep up to get last value
        actionMode?.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
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
                R.id.action_start_exercise_in_walking_mode -> {
                    controller?.dispatch(StartExerciseInWalkingModeMenuItemClicked)
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
        const val STATE_KEY_FILTER_DIALOG = "filterDialog"
        const val STATE_KEY_SEARCH_VIEW_TEXT = "searchViewText"
    }
}