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
import com.odnovolov.forgetmenot.screen.home.decksorting.DeckSorting.*
import com.odnovolov.forgetmenot.screen.home.decksorting.DeckSortingEvent.SortByButtonClicked
import com.odnovolov.forgetmenot.screen.home.decksorting.DeckSortingOrder.DismissBottomSheet
import kotlinx.android.synthetic.main.bottom_sheet_deck_sorting.*
import kotlinx.coroutines.launch

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
        takeOrders()
    }

    private fun setupView() {
        setOnClickListeners()
        setBottomSheetAlwaysExpanded()
    }

    private fun setOnClickListeners() {
        sortByLastCreatedTextView.setOnClickListener {
            controller.dispatch(SortByButtonClicked(BY_LAST_CREATED))
        }
        sortByNameTextView.setOnClickListener {
            controller.dispatch(SortByButtonClicked(BY_NAME))
        }
        sortByLastOpenedTextView.setOnClickListener {
            controller.dispatch(SortByButtonClicked(BY_LAST_OPENED))
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
        viewModel.deckSorting.observe { deckSorting ->
            setupSortingTextView(
                sortByLastCreatedTextView,
                isChecked = deckSorting == BY_LAST_CREATED
            )
            setupSortingTextView(
                sortByNameTextView,
                isChecked = deckSorting == BY_NAME
            )
            setupSortingTextView(
                sortByLastOpenedTextView,
                isChecked = deckSorting == BY_LAST_OPENED
            )
        }
    }

    private fun setupSortingTextView(textView: TextView, isChecked: Boolean) {
        textView.isClickable = !isChecked
        textView.setCompoundDrawablesWithIntrinsicBounds(
            0, 0, if (isChecked) R.drawable.ic_check_blue_24dp else 0, 0
        )
    }

    private fun takeOrders() {
        fragmentScope.launch {
            for (order in controller.orders) {
                when (order) {
                    DismissBottomSheet -> dismiss()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
    }

}