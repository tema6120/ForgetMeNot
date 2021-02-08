package com.odnovolov.forgetmenot.presentation.screen.fileimport.cards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.R.plurals
import com.odnovolov.forgetmenot.domain.interactor.fileimport.CardPrototype
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cards.ImportedCardsEvent.CardClicked
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cards.ImportedCardsEvent.SelectAllButtonClicked
import kotlinx.android.synthetic.main.fragment_imported_cards.*
import kotlinx.coroutines.launch

class ImportedCardsFragment : BaseFragment() {
    init {
        ImportedCardsDiScope.reopenIfClosed()
    }

    private var controller: ImportedCardsController? = null
    private lateinit var viewModel: ImportedCardsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_imported_cards, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = ImportedCardsDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            observeViewModel()
        }
    }

    private fun setupView() {
        val onCardClicked: (Long) -> Unit = { id: Long ->
            controller?.dispatch(CardClicked(id))
        }
        cardsRecycler.adapter = CardPrototypeAdapter(onCardClicked)
        selectAllButton.setOnClickListener {
            controller?.dispatch(SelectAllButtonClicked)
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            cardPrototypes.observe { cardPrototypes: List<CardPrototype> ->
                (cardsRecycler.adapter as CardPrototypeAdapter).submitList(cardPrototypes)
            }
            numberOfSelectedCards.observe { numberOfSelectedCards: Int ->
                numberOfSelectedCardsTextView.text = resources.getQuantityString(
                    plurals.title_cards_selection_toolbar_in_file_import,
                    numberOfSelectedCards,
                    numberOfSelectedCards
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cardsRecycler.adapter = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            ImportedCardsDiScope.close()
        }
    }
}