package com.odnovolov.forgetmenot.presentation.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseBottomSheetDialogFragment
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.*
import kotlinx.android.synthetic.main.bottom_sheet_card_selection_options.*
import kotlinx.coroutines.launch

class CardSelectionOptionsBottomSheet : BaseBottomSheetDialogFragment() {
    init {
        HomeDiScope.reopenIfClosed()
    }

    private var controller: HomeController? = null

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
        viewCoroutineScope!!.launch {
            val diScope = HomeDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            observeViewModel(diScope.viewModel)
        }
    }

    private fun setupView() {
        invertOptionItem.setOnClickListener {
            controller?.dispatch(InvertCardSelectionOptionSelected)
            dismiss()
        }
        changeGradeOptionItem.setOnClickListener {
            controller?.dispatch(ChangeGradeCardSelectionOptionSelected)
            dismiss()
        }
        markAsLearnedOptionItem.setOnClickListener {
            controller?.dispatch(MarkAsLearnedCardSelectionOptionSelected)
            dismiss()
        }
        markAsUnlearnedOptionItem.setOnClickListener {
            controller?.dispatch(MarkAsUnlearnedCardSelectionOptionSelected)
            dismiss()
        }
        removeOptionItem.setOnClickListener {
            controller?.dispatch(RemoveCardsCardSelectionOptionSelected)
            dismiss()
        }
        moveOptionItem.setOnClickListener {
            controller?.dispatch(MoveCardSelectionOptionSelected)
            dismiss()
        }
        copyOptionItem.setOnClickListener {
            controller?.dispatch(CopyCardSelectionOptionSelected)
            dismiss()
        }
    }

    private fun observeViewModel(viewModel: HomeViewModel) {
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
}