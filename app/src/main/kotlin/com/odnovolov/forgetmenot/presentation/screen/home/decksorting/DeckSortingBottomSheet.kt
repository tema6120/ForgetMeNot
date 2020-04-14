package com.odnovolov.forgetmenot.presentation.screen.home.decksorting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseBottomSheetDialogFragment
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Criterion.*
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Direction
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Direction.Asc
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSorting.Direction.Desc
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSortingCommand.DismissBottomSheet
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSortingEvent.SortByButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.home.needToCloseDiScope
import kotlinx.android.synthetic.main.bottom_sheet_deck_sorting.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeckSortingBottomSheet : BaseBottomSheetDialogFragment() {
    init {
        DeckSortingDiScope.reopenIfClosed()
    }

    private var controller: DeckSortingController? = null

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
        viewScope!!.launch(Dispatchers.Main.immediate) {
            val diScope = DeckSortingDiScope.get()
            controller = diScope.controller
            val viewModel = diScope.viewModel
            observeViewModel(viewModel)
            controller!!.commands.observe(::executeCommand)
        }
    }

    private fun setupView() {
        setOnClickListeners()
        setBottomSheetAlwaysExpanded()
    }

    private fun setOnClickListeners() {
        sortByNameButton.setOnClickListener {
            controller?.dispatch(SortByButtonClicked(Name))
        }
        sortByTimeCreatedButton.setOnClickListener {
            controller?.dispatch(SortByButtonClicked(CreatedAt))
        }
        sortByTimeLastOpenedButton.setOnClickListener {
            controller?.dispatch(SortByButtonClicked(LastOpenedAt))
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

    private fun observeViewModel(viewModel: DeckSortingViewModel) {
        viewModel.deckSorting.observe {
            setupSortingButton(
                sortByNameButton,
                direction = if (it.criterion === Name) it.direction else null
            )
            setupSortingButton(
                sortByTimeCreatedButton,
                direction = if (it.criterion === CreatedAt) it.direction else null
            )
            setupSortingButton(
                sortByTimeLastOpenedButton,
                direction = if (it.criterion === LastOpenedAt) it.direction else null
            )
        }
    }

    private fun setupSortingButton(textView: TextView, direction: Direction?) {
        val resId = when (direction) {
            null -> R.drawable.transparent_24dp
            Asc -> R.drawable.ic_arrow_upward_dark_24dp
            Desc -> R.drawable.ic_arrow_downward_dark_24dp
        }
        textView.setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0)
        textView.isSelected = direction != null
    }

    private fun executeCommand(command: DeckSortingCommand) {
        when (command) {
            DismissBottomSheet -> dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            DeckSortingDiScope.close()
        }
    }
}