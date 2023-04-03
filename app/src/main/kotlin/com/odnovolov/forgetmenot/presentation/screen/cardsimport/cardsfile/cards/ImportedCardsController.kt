package com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.cards

import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsImporter
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsImporter.State
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.cards.ImportedCardsEvent.*

class ImportedCardsController(
    private val cardsImporter: CardsImporter,
    private val longTermStateSaver: LongTermStateSaver,
    private val cardsImporterStateProvider: ShortTermStateProvider<State>
) : BaseController<ImportedCardsEvent, Nothing>() {
    override fun handle(event: ImportedCardsEvent) {
        when (event) {
            is CardClicked -> {
                cardsImporter.invertSelection(event.id)
            }

            SelectAllButtonClicked -> {
                cardsImporter.selectAll()
            }

            UnselectAllButtonClicked -> {
                cardsImporter.unselectAll()
            }

            SelectOnlyNewButtonClicked -> {
                cardsImporter.selectOnlyNew()
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        cardsImporterStateProvider.save(cardsImporter.state)
    }
}