package com.odnovolov.forgetmenot.presentation.screen.home

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.widget.TooltipCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.R.drawable
import com.odnovolov.forgetmenot.R.string
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.Item
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemAdapter
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemForm.AsCheckBox
import com.odnovolov.forgetmenot.presentation.common.dp
import com.odnovolov.forgetmenot.presentation.common.observe
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Criterion.*
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Asc
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Desc
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.*
import kotlinx.android.synthetic.main.fragment_deck_list.*
import kotlinx.android.synthetic.main.item_deck_preview_header.view.*
import kotlinx.android.synthetic.main.popup_deck_sorting.view.*
import kotlinx.coroutines.*

class DeckListFragment : BaseFragment() {
    init {
        HomeDiScope.reopenIfClosed()
    }

    private lateinit var viewModel: HomeViewModel
    private var controller: HomeController? = null
    private lateinit var deckPreviewAdapter: DeckPreviewAdapter
    private var filterDialog: Dialog? = null
    private var sortingPopup: PopupWindow? = null
    private lateinit var filterAdapter: ItemAdapter
    private var resumePauseCoroutineScope: CoroutineScope? = null
    var scrollListener: ((dy: Int) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initFilterDialog()
        initSortingPopup()
        return inflater.inflate(R.layout.fragment_deck_list, container, false)
    }

    private fun initSortingPopup() {
        val content = View.inflate(requireContext(), R.layout.popup_deck_sorting, null)
            .apply {
                closeButton.setOnClickListener {
                    sortingPopup?.dismiss()
                }
                TooltipCompat.setTooltipText(closeButton, closeButton.contentDescription)
                sortByNameButton.setOnClickListener {
                    controller?.dispatch(SortByButtonClicked(Name))
                }
                sortByTimeCreatedButton.setOnClickListener {
                    controller?.dispatch(SortByButtonClicked(CreatedAt))
                }
                sortByTimeLastTestedButton.setOnClickListener {
                    controller?.dispatch(SortByButtonClicked(LastOpenedAt))
                }
                sortingDirectionButton.setOnClickListener {
                    controller?.dispatch(SortingDirectionButtonClicked)
                }
                TooltipCompat.setTooltipText(
                    sortingDirectionButton,
                    sortingDirectionButton.contentDescription
                )
            }
        content.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        sortingPopup = PopupWindow(context).apply {
            width = content.measuredWidth
            height = content.measuredHeight
            contentView = content
            setBackgroundDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.background_popup_light
                )
            )
            elevation = 20f.dp
            isOutsideTouchable = true
            isFocusable = true
            animationStyle = R.style.PopupAnimation
        }
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
                showSortingPopup(anchor = header.sortingButton)
            }
            viewModel.deckSorting.observe { deckSorting: DeckSorting ->
                updateSortingButton(header.sortingButton, deckSorting)
                updateSortingPopup(deckSorting)
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

    private fun updateSortingButton(
        sortingButton: TextView,
        deckSorting: DeckSorting
    ) {
        sortingButton.text = getString(
            when (deckSorting.criterion) {
                Name -> string.sort_by_name
                CreatedAt -> string.sort_by_time_created
                LastOpenedAt -> string.sort_by_time_last_tested
            }
        )
        val directionIconId: Int = when (deckSorting.direction) {
            Asc -> drawable.ic_round_arrow_upward_16
            Desc -> drawable.ic_round_arrow_downward_16
        }
        sortingButton.setCompoundDrawablesWithIntrinsicBounds(
            drawable.ic_sorting, 0, directionIconId, 0
        )
    }

    private fun updateSortingPopup(deckSorting: DeckSorting) {
        sortingPopup?.contentView?.run {
            sortingDirectionButton.setImageResource(
                when (deckSorting.direction) {
                    Asc -> R.drawable.ic_round_arrow_upward_24
                    Desc -> R.drawable.ic_round_arrow_downward_24
                }
            )
            val directionButtonAnchor: View = when (deckSorting.criterion) {
                Name -> sortByNameTextView
                CreatedAt -> sortByTimeCreatedTextView
                LastOpenedAt -> sortByTimeLastTestedTextView
            }
            sortingDirectionButton.updateLayoutParams<ConstraintLayout.LayoutParams> {
                topToTop = directionButtonAnchor.id
                bottomToBottom = directionButtonAnchor.id
            }
            sortByNameTextView.isSelected = deckSorting.criterion == Name
            sortByTimeCreatedTextView.isSelected = deckSorting.criterion == CreatedAt
            sortByTimeLastTestedTextView.isSelected = deckSorting.criterion == LastOpenedAt
        }
    }

    private fun showSortingPopup(anchor: View) {
        val anchorLocation = IntArray(2).also(anchor::getLocationOnScreen)
        val x: Int = anchorLocation[0] + anchor.width - sortingPopup!!.width
        val y: Int = anchorLocation[1]
        sortingPopup!!.showAtLocation(anchor.rootView, Gravity.NO_GRAVITY, x, y)
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
        sortingPopup = null
    }
}