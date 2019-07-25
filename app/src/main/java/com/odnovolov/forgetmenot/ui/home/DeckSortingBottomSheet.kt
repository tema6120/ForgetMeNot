package com.odnovolov.forgetmenot.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.ui.home.DeckSorting.*
import com.odnovolov.forgetmenot.ui.home.HomeViewModel.Action.DismissDeckSortingBottomSheet
import com.odnovolov.forgetmenot.ui.home.HomeViewModel.Event.*
import kotlinx.android.synthetic.main.bottom_sheet_deck_sorting.*

class DeckSortingBottomSheet : BottomSheetDialogFragment() {

    lateinit var viewModel: HomeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_deck_sorting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupView()
        subscribeToViewModel()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupView() {
        setOnClickListeners()
        setBottomSheetAlwaysExpanded()
    }

    private fun setOnClickListeners() {
        sortByNameTextView.setOnClickListener { viewModel.onEvent(SortByNameTextViewClicked) }
        sortByTimeCreatedTextView.setOnClickListener { viewModel.onEvent(SortByTimeCreatedTextViewClicked) }
        sortByLastOpenedTextView.setOnClickListener { viewModel.onEvent(SortByLastOpenedTextViewClicked) }
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

    private fun subscribeToViewModel() {
        with(viewModel.state) {
            deckSorting.observe(viewLifecycleOwner, Observer { deckSorting ->
                setupSortingTextView(sortByTimeCreatedTextView, isChecked = deckSorting == BY_TIME_CREATED)
                setupSortingTextView(sortByNameTextView, isChecked = deckSorting == BY_NAME)
                setupSortingTextView(sortByLastOpenedTextView, isChecked = deckSorting == BY_LAST_OPENED)
            })
        }

        viewModel.action?.observe(this, Observer { action ->
            when (action) {
                DismissDeckSortingBottomSheet -> dismiss()
            }
        })
    }

    private fun setupSortingTextView(textView: TextView, isChecked: Boolean) {
        textView.isClickable = !isChecked
        textView.setCompoundDrawablesWithIntrinsicBounds(
            0,
            0,
            if (isChecked) R.drawable.ic_check_blue_24dp else 0,
            0
        )
    }

}