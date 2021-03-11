package com.odnovolov.forgetmenot.presentation.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseBottomSheetDialogFragment
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.*
import kotlinx.android.synthetic.main.bottom_sheet_deck_selection_options.*
import kotlinx.coroutines.launch

class DeckSelectionOptionsBottomSheet : BaseBottomSheetDialogFragment() {
    init {
        HomeDiScope.reopenIfClosed()
    }

    private var controller: HomeController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_deck_selection_options, container, false)
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
        pinDeckSelectionOptionItem.setOnClickListener {
            controller?.dispatch(PinDeckSelectionOptionSelected)
            dismiss()
        }
        unpinDeckSelectionOptionItem.setOnClickListener {
            controller?.dispatch(UnpinDeckSelectionOptionSelected)
            dismiss()
        }
        addToDeckListDeckSelectionOptionItem.setOnClickListener {
            controller?.dispatch(AddToDeckListDeckSelectionOptionSelected)
            dismiss()
        }
        exportDeckSelectionOptionItem.setOnClickListener {
            controller?.dispatch(ExportDeckSelectionOptionSelected)
            dismiss()
        }
        mergeIntoDeckSelectionOptionItem.setOnClickListener {
            controller?.dispatch(MergeIntoDeckSelectionOptionSelected)
            dismiss()
        }
        removeDeckSelectionOptionItem.setOnClickListener {
            controller?.dispatch(RemoveDeckSelectionOptionSelected)
            dismiss()
        }
    }

    private fun observeViewModel(viewModel: HomeViewModel) {
        with(viewModel) {
            numberOfSelectedDecks.observe { numberOfSelectedDecks: Int ->
                numberOfSelectedDecksTextView.text = resources.getQuantityString(
                    R.plurals.title_selection_toolbar_number_of_selected_decks,
                    numberOfSelectedDecks,
                    numberOfSelectedDecks
                )
            }
            isPinDeckSelectionOptionAvailable.observe { isAvailable: Boolean ->
                pinDeckSelectionOptionItem.isVisible = isAvailable
            }
            isUnpinDeckSelectionOptionAvailable.observe { isAvailable: Boolean ->
                unpinDeckSelectionOptionItem.isVisible = isAvailable
            }
        }
    }
}