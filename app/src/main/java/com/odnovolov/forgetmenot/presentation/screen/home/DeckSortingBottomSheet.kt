package com.odnovolov.forgetmenot.presentation.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.DeckSorting.*
import com.odnovolov.forgetmenot.presentation.common.MviBottomSheetDialogFragment
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.*
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.News.DismissDeckSortingBottomSheet
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.UiEvent.*
import com.odnovolov.forgetmenot.presentation.screen.home.di.HomeScreenComponent
import kotlinx.android.synthetic.main.bottom_sheet_deck_sorting.*
import javax.inject.Inject

class DeckSortingBottomSheet : MviBottomSheetDialogFragment<ViewState, UiEvent, News>() {

    @Inject lateinit var bindings: DeckSortingBottomSheetBindings

    override fun onCreate(savedInstanceState: Bundle?) {
        HomeScreenComponent.get()!!.inject(this)
        super.onCreate(savedInstanceState)
        bindings.setup(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bottom_sheet_deck_sorting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setOnClickListeners()
        setBottomSheetAlwaysExpanded()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setOnClickListeners() {
        sortByNameTextView.setOnClickListener { emitEvent(SortByNameTextViewClicked) }
        sortByTimeCreatedTextView.setOnClickListener { emitEvent(SortByTimeCreatedTextViewClicked) }
        sortByLastOpenedTextView.setOnClickListener { emitEvent(SortByLastOpenedTextViewClicked) }
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

    override fun accept(viewState: ViewState) {
        setupSortingTextView(sortByNameTextView, viewState.deckSorting == BY_NAME)
        setupSortingTextView(sortByTimeCreatedTextView, viewState.deckSorting == BY_TIME_CREATED)
        setupSortingTextView(sortByLastOpenedTextView, viewState.deckSorting == BY_LAST_OPENED)
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

    override fun acceptNews(news: News) {
        when (news) {
            DismissDeckSortingBottomSheet -> dismiss()
        }
    }

}