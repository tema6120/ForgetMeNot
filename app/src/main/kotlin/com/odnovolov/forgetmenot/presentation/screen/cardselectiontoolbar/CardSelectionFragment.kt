package com.odnovolov.forgetmenot.presentation.screen.cardselectiontoolbar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.cardselectiontoolbar.CardSelectionEvent.CancelledSelection
import com.odnovolov.forgetmenot.presentation.screen.cardselectiontoolbar.CardSelectionEvent.RemoveCardsOptionSelected
import kotlinx.android.synthetic.main.toolbar_card_selection.*

class CardSelectionFragment : BaseFragment() {
    private var controller: CardSelectionController? = null
    private var viewModel: CardSelectionViewModel? = null
    private var onSelectAllButtonClicked: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.toolbar_card_selection, container, false)
    }

    fun inject(
        controller: CardSelectionController,
        viewModel: CardSelectionViewModel,
        onSelectAllButtonClicked: () -> Unit
    ) {
        this.controller = controller
        this.viewModel = viewModel
        this.onSelectAllButtonClicked = onSelectAllButtonClicked
        observeViewModelIfPossible()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModelIfPossible()
    }

    private fun setupView() {
        cancelSelectionButton.setOnClickListener {
            controller?.dispatch(CancelledSelection)
        }
        selectAllButton.setOnClickListener {
            onSelectAllButtonClicked?.invoke()
        }
        removeCardsButton.setOnClickListener {
            controller?.dispatch(RemoveCardsOptionSelected)
        }
        moreOptionsButton.setOnClickListener {
            if (controller != null) {
                CardSelectionOptionsBottomSheet()
                    .show(childFragmentManager, "CardSelectionOptionsBottomSheet")
            }
        }
    }

    private fun observeViewModelIfPossible() {
        if (viewCoroutineScope == null || viewModel == null) return
        with(viewModel!!) {
            numberOfSelectedCards.observe { numberOfSelectedCards: Int ->
                numberOfSelectedCardsTextView.text =
                    resources.getQuantityString(
                        R.plurals.title_card_selection_toolbar,
                        numberOfSelectedCards,
                        numberOfSelectedCards
                    )
            }
        }
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        when (childFragment) {
            is CardSelectionOptionsBottomSheet -> {
                childFragment.controller = controller!!
                childFragment.viewModel = viewModel!!
            }
        }
    }
}