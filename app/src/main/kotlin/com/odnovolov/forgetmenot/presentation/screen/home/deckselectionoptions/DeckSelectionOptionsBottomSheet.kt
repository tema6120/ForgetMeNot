package com.odnovolov.forgetmenot.presentation.screen.home.deckselectionoptions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseBottomSheetDialogFragment
import com.odnovolov.forgetmenot.presentation.screen.home.HomeController
import com.odnovolov.forgetmenot.presentation.screen.home.HomeDiScope
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.*
import kotlinx.android.synthetic.main.bottom_sheet_deck_options.*
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
            observeViewModel(diScope.deckSelectionOptionsViewModel)
        }
    }

    private fun setupView() {
        pinDeckSelectionOptionItem.setOnClickListener {
            controller?.dispatch(PinDeckSelectionOptionWasSelected)
            dismiss()
        }
        unpinDeckSelectionOptionItem.setOnClickListener {
            controller?.dispatch(UnpinDeckSelectionOptionWasSelected)
            dismiss()
        }
        addToDeckListDeckSelectionOptionItem.setOnClickListener {
            controller?.dispatch(AddToDeckListDeckSelectionOptionWasSelected)
            dismiss()
        }
        removeFromDeckListDeckSelectionOptionItem.setOnClickListener {
            controller?.dispatch(RemoveFromDeckListDeckSelectionOptionWasSelected)
            dismiss()
        }
        setPresetDeckSelectionOptionItem.setOnClickListener {
            controller?.dispatch(SetPresetDeckSelectionOptionWasSelected)
            dismiss()
        }
        exportDeckSelectionOptionItem.setOnClickListener {
            controller?.dispatch(ExportDeckSelectionOptionWasSelected)
            dismiss()
        }
        mergeIntoDeckSelectionOptionItem.setOnClickListener {
            controller?.dispatch(MergeIntoDeckSelectionOptionWasSelected)
            dismiss()
        }
        removeDeckSelectionOptionItem.setOnClickListener {
            controller?.dispatch(RemoveDeckSelectionOptionWasSelected)
            dismiss()
        }
    }

    private fun observeViewModel(viewModel: DeckSelectionOptionsViewModel) {
        with(viewModel) {
            numberOfSelectedDecks.observe { numberOfSelectedDecks: Int ->
                numberOfSelectedDecksTextView.text = resources.getQuantityString(
                    R.plurals.title_selection_toolbar_number_of_selected_decks,
                    numberOfSelectedDecks,
                    numberOfSelectedDecks
                )
            }
            canBePinned.observe { canBePinned: Boolean ->
                pinDeckSelectionOptionItem.isVisible = canBePinned
            }
            canBeUnpinned.observe { canBeUnpinned: Boolean ->
                unpinDeckSelectionOptionItem.isVisible = canBeUnpinned
            }
            namesOfDeckListsToWhichDecksBelong.observe { namesOfDeckLists: List<String> ->
                removeFromDeckListDeckSelectionOptionItem.isVisible = namesOfDeckLists.isNotEmpty()
                if (namesOfDeckLists.size == 1) {
                    removeFromDeckListDeckSelectionOptionItem.text = getString(
                        R.string.deck_option_remove_from_deck_list_with_arg,
                        namesOfDeckLists[0]
                    )
                }

            }
        }
    }
}