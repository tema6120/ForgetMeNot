package com.odnovolov.forgetmenot.presentation.screen.home

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.Item
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemAdapter
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemForm.AsCheckBox
import com.odnovolov.forgetmenot.presentation.common.observe
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.DisplayOnlyWithTasksCheckboxClicked
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Criterion.*
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Direction.Asc
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Direction.Desc
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSortingBottomSheet
import kotlinx.android.synthetic.main.fragment_deck_list.*
import kotlinx.android.synthetic.main.item_deck_preview_header.view.*
import kotlinx.coroutines.*

class DeckListFragment : BaseFragment() {
    init {
        HomeDiScope.reopenIfClosed()
    }

    private lateinit var viewModel: HomeViewModel
    private var controller: HomeController? = null
    private lateinit var deckPreviewAdapter: DeckPreviewAdapter
    private var filterDialog: Dialog? = null
    private lateinit var filterAdapter: ItemAdapter
    private var resumePauseCoroutineScope: CoroutineScope? = null
    var scrollListener: ((dy: Int) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initFilterDialog()
        return inflater.inflate(R.layout.fragment_deck_list, container, false)
    }

    private fun initFilterDialog() {
        filterDialog = ChoiceDialogCreator.create(
            context = requireContext(),
            title = getString(R.string.title_deckpreview_filter_dialog),
            itemForm = AsCheckBox,
            onItemClick = { controller?.dispatch(DisplayOnlyWithTasksCheckboxClicked) },
            takeAdapter = { filterAdapter = it }
        )
        dialogTimeCapsule.register("filterDialog", filterDialog!!)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = HomeDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            initDeckPreviewAdapter()
            observeViewModel()
        }
    }

    private fun setupView() {
        decksPreviewRecycler.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val canScrollUp = decksPreviewRecycler.canScrollVertically(-1)
                divider.isVisible = canScrollUp
                scrollListener?.invoke(dy)
            }
        })
    }

    private fun initDeckPreviewAdapter() {
        val setupHeader: (View) -> Unit = { header: View ->
            header.filterButton.setOnClickListener {
                filterDialog?.show()
            }
            header.sortingButton.setOnClickListener {
                DeckSortingBottomSheet()
                    .show(childFragmentManager, "DeckSortingBottomSheet Tag")
            }
            viewModel.deckSorting.observe { deckSorting: DeckSorting ->
                header.sortingButton.text = getString(
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
                header.sortingButton.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_sorting, 0, directionIconId, 0
                )
            }
        }
        deckPreviewAdapter = DeckPreviewAdapter(
            controller!!,
            setupHeader,
            viewModel.deckSelection,
            viewCoroutineScope!!
        )
        decksPreviewRecycler.adapter = deckPreviewAdapter
    }

    private fun observeViewModel() {
        with(viewModel) {
            displayOnlyWithTasks.observe { displayOnlyDecksAvailableForExercise: Boolean ->
                val item = object : Item {
                    override val text = getString(R.string.filter_display_only_with_tasks)
                    override val isSelected = displayOnlyDecksAvailableForExercise
                }
                filterAdapter.submitList(listOf(item))
            }
            decksNotFound.observe { decksNotFound: Boolean ->
                emptyTextView.isVisible = decksNotFound
                progressBar.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        resumePauseCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        resumePauseCoroutineScope!!.launch {
            val diScope = HomeDiScope.getAsync() ?: return@launch
            val viewModel = diScope.viewModel
            with(viewModel) {
                deckListItem.observe(resumePauseCoroutineScope!!) { deckListItem: List<DeckListItem> ->
                    deckPreviewAdapter.submitList(deckListItem)
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        resumePauseCoroutineScope!!.cancel()
        resumePauseCoroutineScope = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        filterDialog = null
        decksPreviewRecycler.adapter = null
    }
}