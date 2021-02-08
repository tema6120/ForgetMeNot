package com.odnovolov.forgetmenot.presentation.screen.fileimport.cards

import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImporter
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cards.ImportedCardsEvent.CardClicked
import com.odnovolov.forgetmenot.presentation.screen.fileimport.cards.ImportedCardsEvent.SelectAllButtonClicked

class ImportedCardsController(
    private val fileImporter: FileImporter,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<ImportedCardsEvent, Nothing>() {
    override fun handle(event: ImportedCardsEvent) {
        when (event) {
            is CardClicked -> {
                fileImporter.invertSelection(event.id)
            }

            SelectAllButtonClicked -> {
                fileImporter.selectAll()
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}