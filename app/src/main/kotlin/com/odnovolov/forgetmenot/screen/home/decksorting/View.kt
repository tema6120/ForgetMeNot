package com.odnovolov.forgetmenot.screen.home.decksorting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.base.BaseBottomSheetDialogFragment
import com.odnovolov.forgetmenot.screen.home.decksorting.DeckSorting.Criterion.*
import com.odnovolov.forgetmenot.screen.home.decksorting.DeckSorting.Direction
import com.odnovolov.forgetmenot.screen.home.decksorting.DeckSorting.Direction.ASC
import com.odnovolov.forgetmenot.screen.home.decksorting.DeckSorting.Direction.DESC
import com.odnovolov.forgetmenot.screen.home.decksorting.DeckSortingEvent.SortByButtonClicked
import com.odnovolov.forgetmenot.screen.home.decksorting.DeckSortingOrder.DismissBottomSheet
import kotlinx.android.synthetic.main.bottom_sheet_deck_sorting.*

class DeckSortingBottomSheet : BaseBottomSheetDialogFragment() {

    private val controller = DeckSortingController()
    private val viewModel = DeckSortingViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_deck_sorting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        controller.orders.forEach(::executeOrder)
    }

    private fun setupView() {
        setOnClickListeners()
        setBottomSheetAlwaysExpanded()
    }

    private fun setOnClickListeners() {
        sortByNameButton.setOnClickListener {
            controller.dispatch(SortByButtonClicked(NAME))
        }
        sortByLastCreatedButton.setOnClickListener {
            controller.dispatch(SortByButtonClicked(CREATED_AT))
        }
        sortByLastOpenedButton.setOnClickListener {
            controller.dispatch(SortByButtonClicked(LAST_OPENED_AT))
        }
    }

    private fun setBottomSheetAlwaysExpanded() {
        dialog?.setOnShowListener { dialog1 ->
            val bottomSheetDialog = dialog1 as BottomSheetDialog
            val bottomSheet: FrameLayout? =
                bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    private fun observeViewModel() {
        viewModel.deckSorting.observe {
            setupSortingTextView(
                sortByNameTextView,
                direction = if (it.criterion === NAME) it.direction else null
            )
            setupSortingTextView(
                sortByLastCreatedTextView,
                direction = if (it.criterion === CREATED_AT) it.direction else null
            )
            setupSortingTextView(
                sortByLastOpenedTextView,
                direction = if (it.criterion === LAST_OPENED_AT) it.direction else null
            )
        }
    }

    private fun setupSortingTextView(textView: TextView, direction: Direction?) {
        val resId = when (direction) {
            null -> R.drawable.transparent_24dp
            ASC -> R.drawable.ic_arrow_upward_dark_24dp
            DESC -> R.drawable.ic_arrow_downward_dark_24dp
        }
        textView.setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0)
        textView.isSelected = direction != null
    }

    private fun executeOrder(order: DeckSortingOrder) {
        when (order) {
            DismissBottomSheet -> dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
    }

}