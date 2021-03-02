package com.odnovolov.forgetmenot.presentation.screen.cardselectiontoolbar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseBottomSheetDialogFragment
import com.odnovolov.forgetmenot.presentation.screen.cardselectiontoolbar.CardSelectionEvent.*
import kotlinx.android.synthetic.main.bottom_sheet_card_selection_options.*

class CardSelectionOptionsBottomSheet : BaseBottomSheetDialogFragment() {
    lateinit var controller: CardSelectionController
    lateinit var viewModel: CardSelectionViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_card_selection_options, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
    }

    private fun setupView() {
        invertOptionItem.setOnClickListener {
            controller.dispatch(InvertOptionSelected)
            dismiss()
        }
        changeGradeOptionItem.setOnClickListener {
            controller.dispatch(ChangeGradeOptionSelected)
            dismiss()
        }
        markAsLearnedOptionItem.setOnClickListener {
            controller.dispatch(MarkAsLearnedOptionSelected)
            dismiss()
        }
        markAsUnlearnedOptionItem.setOnClickListener {
            controller.dispatch(MarkAsUnlearnedOptionSelected)
            dismiss()
        }
        removeOptionItem.setOnClickListener {
            controller.dispatch(RemoveCardsOptionSelected)
            dismiss()
        }
        moveOptionItem.setOnClickListener {
            controller.dispatch(MoveOptionSelected)
            dismiss()
        }
        copyOptionItem.setOnClickListener {
            controller.dispatch(CopyOptionSelected)
            dismiss()
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            numberOfSelectedCards.observe { numberOfSelectedCards: Int ->
                numberOfSelectedItemsTextView.text =
                    resources.getQuantityString(
                        R.plurals.title_card_selection_toolbar,
                        numberOfSelectedCards,
                        numberOfSelectedCards
                    )
            }
            markAsLearnedOptionItem.isVisible = isMarkAsLearnedOptionAvailable
            markAsUnlearnedOptionItem.isVisible = isMarkAsUnlearnedOptionAvailable
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
    }
}