package com.odnovolov.forgetmenot.presentation.screen.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseBottomSheetDialogFragment
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.*
import kotlinx.android.synthetic.main.bottom_sheet_deck_options.*
import kotlinx.coroutines.launch

class DeckOptionsBottomSheet : BaseBottomSheetDialogFragment() {
    init {
        HomeDiScope.reopenIfClosed()
    }

    private var controller: HomeController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_deck_options, container, false)
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
        startExerciseDeckOptionItem.setOnClickListener {
            controller?.dispatch(StartExerciseDeckOptionSelected)
            dismiss()
        }
        autoplayDeckOptionItem.setOnClickListener {
            controller?.dispatch(AutoplayDeckOptionSelected)
            dismiss()
        }
        renameDeckOptionItem.setOnClickListener {
            controller?.dispatch(RenameDeckOptionSelected)
            dismiss()
        }
        editCardsDeckOptionItem.setOnClickListener {
            controller?.dispatch(EditCardsDeckOptionSelected)
            dismiss()
        }
        setupDeckOptionItem.setOnClickListener {
            controller?.dispatch(SetupDeckOptionSelected)
            dismiss()
        }
        exportDeckOptionItem.setOnClickListener {
            controller?.dispatch(ExportDeckOptionSelected)
            dismiss()
        }
        mergeIntoDeckOptionItem.setOnClickListener {
            controller?.dispatch(MergeIntoDeckOptionSelected)
            dismiss()
        }
        removeDeckOptionItem.setOnClickListener {
            controller?.dispatch(RemoveDeckOptionSelected)
            dismiss()
        }
    }

    private fun observeViewModel(viewModel: HomeViewModel) {
        viewModel.deckNameInDeckOptionMenu.observe { deckName: String? ->
            if (deckName != null) {
                deckNameTextView.text = deckName
            }
        }
    }
}