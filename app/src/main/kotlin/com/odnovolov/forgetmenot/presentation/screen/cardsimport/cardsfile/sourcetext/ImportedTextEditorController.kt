package com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.sourcetext

import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsImporter
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsImporter.State
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.cardsfile.sourcetext.ImportedTextEditorEvent.EncodingWasSelected

class ImportedTextEditorController(
    private val cardsImporter: CardsImporter,
    private val longTermStateSaver: LongTermStateSaver,
    private val fileImporterStateProvider: ShortTermStateProvider<State>
) : BaseController<ImportedTextEditorEvent, Nothing>() {
    override fun handle(event: ImportedTextEditorEvent) {
        when (event) {
            is EncodingWasSelected -> {
                cardsImporter.setCharset(event.newEncoding)
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        fileImporterStateProvider.save(cardsImporter.state)
    }
}