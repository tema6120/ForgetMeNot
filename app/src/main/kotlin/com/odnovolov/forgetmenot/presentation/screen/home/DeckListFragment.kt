package com.odnovolov.forgetmenot.presentation.screen.home

import android.os.Bundle
import android.view.*
import android.view.View.MeasureSpec
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
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.dp
import com.odnovolov.forgetmenot.presentation.common.observe
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Criterion.*
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Asc
import com.odnovolov.forgetmenot.presentation.screen.home.DeckSorting.Direction.Desc
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.*
import kotlinx.android.synthetic.main.fragment_deck_list.*
import kotlinx.android.synthetic.main.item_deck_preview_header.view.*
import kotlinx.android.synthetic.main.popup_deck_filters.view.*
import kotlinx.android.synthetic.main.popup_deck_sorting.view.*
import kotlinx.android.synthetic.main.popup_deck_sorting.view.closeButton
import kotlinx.coroutines.*

class DeckListFragment : BaseFragment() {
    init {
        HomeDiScope.reopenIfClosed()
    }

    private lateinit var viewModel: HomeViewModel
    private var controller: HomeController? = null
    private lateinit var deckPreviewAdapter: DeckPreviewAdapter
    private var filtersPopup: PopupWindow? = null
    private var sortingPopup: PopupWindow? = null
    private var resumePauseCoroutineScope: CoroutineScope? = null
    var scrollListener: ((dy: Int) -> Unit)? = null
    private var filterButton: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initFiltersPopup()
        initSortingPopup()
        return inflater.inflate(R.layout.fragment_deck_list, container, false)
    }

    private fun initFiltersPopup() {
        val content = View.inflate(requireContext(), R.layout.popup_deck_filters, null)
            .apply {
                closeButton.setOnClickListener {
                    filtersPopup?.dismiss()
                }
                TooltipCompat.setTooltipText(closeButton, closeButton.contentDescription)
                availableForExerciseButton.setOnClickListener {
                    controller?.dispatch(DecksAvailableForExerciseCheckboxClicked)
                }
            }
        filtersPopup = PopupWindow(context).apply {
            width = 250.dp
            height = WindowManager.LayoutParams.WRAP_CONTENT
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
            animationStyle = R.style.LeftPopupAnimation
        }
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
            animationStyle = R.style.RightPopupAnimation
        }
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
            filterButton = header.filterButton
            header.filterButton.setOnClickListener {
                showFiltersPopup(anchor = header.filterButton)
            }
            header.sortingButton.setOnClickListener {
                showSortingPopup(anchor = header.sortingButton)
            }
            viewModel.deckSorting.observe { deckSorting: DeckSorting ->
                updateSortingButton(header.sortingButton, deckSorting)
                updateSortingPopup(deckSorting)
            }
        }
        deckPreviewAdapter = DeckPreviewAdapter(controller!!, setupHeader)
        decksPreviewRecycler.adapter = deckPreviewAdapter
    }

    private fun showFiltersPopup(anchor: View) {
        val anchorLocation = IntArray(2).also(anchor::getLocationOnScreen)
        val x: Int = anchorLocation[0]
        val y: Int = anchorLocation[1]
        filtersPopup!!.showAtLocation(anchor.rootView, Gravity.NO_GRAVITY, x, y)
    }

    private fun showSortingPopup(anchor: View) {
        val anchorLocation = IntArray(2).also(anchor::getLocationOnScreen)
        val x: Int = anchorLocation[0] + anchor.width - sortingPopup!!.width
        val y: Int = anchorLocation[1]
        sortingPopup!!.showAtLocation(anchor.rootView, Gravity.NO_GRAVITY, x, y)
    }

    private fun updateSortingButton(
        sortingButton: TextView,
        deckSorting: DeckSorting
    ) {
        sortingButton.text = getString(
            when (deckSorting.criterion) {
                Name -> R.string.sort_by_name
                CreatedAt -> R.string.sort_by_time_created
                LastOpenedAt -> R.string.sort_by_time_last_tested
            }
        )
        val directionIconId: Int = when (deckSorting.direction) {
            Asc -> R.drawable.ic_round_arrow_upward_16
            Desc -> R.drawable.ic_round_arrow_downward_16
        }
        sortingButton.setCompoundDrawablesWithIntrinsicBounds(
            R.drawable.ic_sorting, 0, directionIconId, 0
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

    private fun observeViewModel() {
        with(viewModel) {
            displayOnlyDecksAvailableForExercise.observe { displayOnlyDecksAvailableForExercise: Boolean ->
                filtersPopup?.contentView?.run {
                    availableForExerciseCheckBox.isChecked = displayOnlyDecksAvailableForExercise
                }
            }
            decksNotFound.observe { decksNotFound: Boolean ->
                emptyTextView.isVisible = decksNotFound
                progressBar.visibility = View.GONE
            }
            deckSelection.observe { deckSelection: DeckSelection? ->
                deckPreviewAdapter.deckSelection = deckSelection
                filterButton?.isVisible = deckSelection == null
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
        decksPreviewRecycler.adapter = null
        filtersPopup = null
        sortingPopup = null
        filterButton = null
    }
}