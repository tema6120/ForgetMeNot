package com.odnovolov.forgetmenot.home

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.base.BaseFragment
import com.odnovolov.forgetmenot.common.viewcreator.ChoiceDialogCreator
import com.odnovolov.forgetmenot.common.viewcreator.ChoiceDialogCreator.Item
import com.odnovolov.forgetmenot.common.viewcreator.ChoiceDialogCreator.ItemAdapter
import com.odnovolov.forgetmenot.common.viewcreator.ChoiceDialogCreator.ItemForm.AsCheckBox
import com.odnovolov.forgetmenot.home.DeckPreviewAdapter.ViewHolder
import com.odnovolov.forgetmenot.home.HomeEvent.*
import com.odnovolov.forgetmenot.home.HomeOrder.*
import com.odnovolov.forgetmenot.home.adddeck.AddDeckFragment
import com.odnovolov.forgetmenot.home.decksorting.DeckSortingBottomSheet
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.item_deck_preview.view.*
import kotlinx.coroutines.launch
import leakcanary.LeakSentry

class HomeFragment : BaseFragment() {

    private val controller = HomeController()
    private val viewModel = HomeViewModel()
    private val adapter = DeckPreviewAdapter(controller)
    private lateinit var filterDialog: Dialog
    private lateinit var filterAdapter: ItemAdapter<Item>

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
        takeOrders()
    }

    private fun setupView() {
        decksPreviewRecycler.adapter = adapter
    }

    private fun observeViewModel() {
        with(viewModel) {
            decksPreview.observe(onChange = adapter::submitList)
            displayOnlyWithTasks.observe { displayOnlyWithTasks: Boolean ->
                val item = object : Item {
                    override val text = getString(R.string.filter_display_only_with_tasks)
                    override val isSelected = displayOnlyWithTasks
                }
                filterAdapter.submitList(listOf(item))
            }
        }
    }

    private fun takeOrders() {
        fragmentScope.launch {
            for (order in controller.orders) {
                when (order) {
                    NavigateToExercise -> {
                        findNavController().navigate(R.id.action_home_screen_to_exercise_screen)
                    }
                    is NavigateToDeckSettings -> {
                        findNavController()
                            .navigate(R.id.action_home_screen_to_deck_settings_screen)
                    }
                    ShowDeckWasDeletedMessage -> {
                        Snackbar
                            .make(
                                homeFragmentRootView,
                                getString(R.string.snackbar_message_deck_is_deleted),
                                resources.getInteger(R.integer.duration_deck_is_deleted_snackbar)
                            )
                            .setAction(
                                R.string.snackbar_action_cancel,
                                { controller.dispatch(DeckIsDeletedSnackbarCancelActionClicked) }
                            )
                            .show()
                    }
                }
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
        LeakSentry.refWatcher.watch(this)
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
            setOnClickListener {
                controller.dispatch(DeckButtonClicked(deckPreview.deckId))
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
                    R.id.deleteDeckMenuItem -> {
                        controller.dispatch(DeleteDeckMenuItemClicked(deckId))
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