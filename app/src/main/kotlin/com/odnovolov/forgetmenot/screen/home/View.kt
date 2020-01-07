package com.odnovolov.forgetmenot.screen.home

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.base.BaseFragment
import com.odnovolov.forgetmenot.common.customview.ChoiceDialogCreator
import com.odnovolov.forgetmenot.common.customview.ChoiceDialogCreator.Item
import com.odnovolov.forgetmenot.common.customview.ChoiceDialogCreator.ItemAdapter
import com.odnovolov.forgetmenot.common.customview.ChoiceDialogCreator.ItemForm.AsCheckBox
import com.odnovolov.forgetmenot.screen.home.DeckPreviewAdapter.ViewHolder
import com.odnovolov.forgetmenot.screen.home.HomeEvent.*
import com.odnovolov.forgetmenot.screen.home.HomeOrder.*
import com.odnovolov.forgetmenot.screen.home.adddeck.AddDeckFragment
import com.odnovolov.forgetmenot.screen.home.decksorting.DeckSortingBottomSheet
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.item_deck_preview.view.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine

class HomeFragment : BaseFragment() {

    private val controller = HomeController()
    private val viewModel = HomeViewModel()
    private val deckPreviewAdapter = DeckPreviewAdapter(controller)
    private lateinit var filterDialog: Dialog
    private lateinit var filterAdapter: ItemAdapter<Item>
    private var actionMode: ActionMode? = null
    private var resumePauseScope: CoroutineScope? = null

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
            onItemClick = { controller.dispatch(DisplayOnlyWithTasksCheckboxClicked) },
            takeAdapter = { filterAdapter = it }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        controller.orders.forEach(::executeOrder)
    }

    private fun setupView() {
        decksPreviewRecycler.adapter = deckPreviewAdapter
    }

    private fun observeViewModel() {
        with(viewModel) {
            displayOnlyWithTasks.observe { displayOnlyWithTasks: Boolean ->
                val item = object : Item {
                    override val text = getString(R.string.filter_display_only_with_tasks)
                    override val isSelected = displayOnlyWithTasks
                }
                filterAdapter.submitList(listOf(item))
            }
            hasAnySelectedDeck.observe { hasAnySelectedDeck ->
                if (hasAnySelectedDeck) {
                    if (actionMode == null) {
                        actionMode = requireActivity().startActionMode(actionModeCallback)
                    }
                } else {
                    actionMode?.finish()
                }
            }
            selectedDecksCount.combine(selectedCardsCount) { decksCount: Int, cardsCount: Long ->
                val deckString = if (decksCount > 1) "decks" else "deck"
                val cardString = if (cardsCount > 1) "cards" else "card"
                "$decksCount $deckString, $cardsCount $cardString"
            }.observe { actionMode?.title = it }
        }
    }

    private fun executeOrder(order: HomeOrder) {
        when (order) {
            ShowNoCardsReadyForExercise -> {
                Toast.makeText(
                    requireContext(),
                    R.string.toast_text_no_cards_ready_for_exercise,
                    Toast.LENGTH_SHORT
                ).show()
            }
            NavigateToExercise -> {
                actionMode?.finish()
                findNavController().navigate(R.id.action_home_screen_to_exercise_screen)
            }
            NavigateToDeckSettings -> {
                actionMode?.finish()
                findNavController().navigate(R.id.action_home_screen_to_deck_settings_screen)
            }
            is ShowDeckRemovingMessage -> {
                Snackbar
                    .make(
                        homeFragmentRootView,
                        resources.getQuantityString(
                            R.plurals.numberOfDecksRemoved,
                            order.numberOfDecksRemoved,
                            order.numberOfDecksRemoved
                        ),
                        resources.getInteger(R.integer.duration_deck_is_deleted_snackbar)
                    )
                    .setAction(
                        R.string.snackbar_action_cancel,
                        { controller.dispatch(DecksRemovedSnackbarCancelActionClicked) }
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
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String) = false

            override fun onQueryTextChange(newText: String): Boolean {
                controller.dispatch(SearchTextChanged(newText))
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
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
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        resumePauseScope = MainScope()
        resumePauseScope!!.launch {
            viewModel.decksPreview.collect {
                if (isActive) {
                    deckPreviewAdapter.submitList(it)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        resumePauseScope!!.cancel()
        resumePauseScope = null
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        decksPreviewRecycler.adapter = null
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
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
                    controller.dispatch(StartExerciseMenuItemClicked)
                    true
                }
                R.id.action_select_all_decks -> {
                    val displayedCardIds: List<Long> =
                        deckPreviewAdapter.currentList.map { it.deckId }
                    controller.dispatch(SelectAllDecksMenuItemClicked(displayedCardIds))
                    true
                }
                R.id.action_remove_decks -> {
                    controller.dispatch(RemoveDecksMenuItemClicked)
                    true
                }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            controller.dispatch(ActionModeFinished)
            actionMode = null
        }
    }

    companion object {
        const val STATE_KEY_FILTER_DIALOG = "filterDialog"
    }
}


private class DeckPreviewAdapter(
    private val controller: HomeController
) : ListAdapter<DeckPreview, ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_deck_preview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        with(viewHolder.itemView) {
            val deckPreview: DeckPreview = getItem(position)
            deckButton.setOnClickListener {
                controller.dispatch(DeckButtonClicked(deckPreview.deckId))
            }
            deckButton.setOnLongClickListener {
                controller.dispatch(DeckButtonLongClicked(deckPreview.deckId))
                true
            }
            deckNameTextView.text = deckPreview.deckName
            deckOptionButton.setOnClickListener { view: View ->
                showOptionMenu(view, deckPreview.deckId)
            }
            passedLapsIndicatorTextView.text = deckPreview.passedLaps.toString()
            val progress = "${deckPreview.learnedCount}/${deckPreview.totalCount}"
            progressIndicatorTextView.text = progress
            if (deckPreview.numberOfCardsReadyForExercise == null) {
                taskIndicatorTextView.visibility = GONE
            } else {
                taskIndicatorTextView.text = deckPreview.numberOfCardsReadyForExercise.toString()
                taskIndicatorTextView.visibility = VISIBLE
            }
            if (deckPreview.isSelected) {
                setBackgroundColor(
                    ContextCompat.getColor(context, R.color.selected_item_background)
                )
            } else {
                background = null
            }
        }
    }

    private fun showOptionMenu(anchor: View, deckId: Long) {
        PopupMenu(anchor.context, anchor).apply {
            inflate(R.menu.deck_preview_actions)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.setupDeckMenuItem -> {
                        controller.dispatch(SetupDeckMenuItemClicked(deckId))
                        true
                    }
                    R.id.removeDeckMenuItem -> {
                        controller.dispatch(RemoveDeckMenuItemClicked(deckId))
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class DiffCallback : DiffUtil.ItemCallback<DeckPreview>() {
        override fun areItemsTheSame(oldItem: DeckPreview, newItem: DeckPreview): Boolean {
            return oldItem.deckId == newItem.deckId
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: DeckPreview, newItem: DeckPreview): Boolean {
            return oldItem == newItem
        }
    }
}