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
import kotlinx.android.synthetic.main.bottom_sheet_deck_sorting.*
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.scope.viewModel

class DeckSortingBottomSheet : BaseBottomSheetDialogFragment() {
    private val koinScope = getKoin().getOrCreateScope<DeckSortingViewModel>(DECK_SORTING_SCOPE_ID)
    private val viewModel: DeckSortingViewModel by koinScope.viewModel(this)
    private val controller: DeckSortingController by koinScope.inject()

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
        controller.commands.observe(::executeCommand)
    }

    private fun setupView() {
        setOnClickListeners()
        setBottomSheetAlwaysExpanded()
    }

    private fun setOnClickListeners() {
        sortByNameButton.setOnClickListener {
            controller.onSortByButtonClicked(Name)
        }
        sortByTimeCreatedButton.setOnClickListener {
            controller.onSortByButtonClicked(CreatedAt)
        }
        sortByTimeLastOpenedButton.setOnClickListener {
            controller.onSortByButtonClicked(LastOpenedAt)
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
                direction = if (it.criterion === Name) it.direction else null
            )
            setupSortingTextView(
                sortByTimeCreatedTextView,
                direction = if (it.criterion === CreatedAt) it.direction else null
            )
            setupSortingTextView(
                sortByTimeLastOpenedTextView,
                direction = if (it.criterion === LastOpenedAt) it.direction else null
            )
        }
    }

    private fun setupSortingTextView(textView: TextView, direction: Direction?) {
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
}