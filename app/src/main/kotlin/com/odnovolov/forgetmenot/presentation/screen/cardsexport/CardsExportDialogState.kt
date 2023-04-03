package com.odnovolov.forgetmenot.presentation.screen.cardsexport

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsFileFormat
import kotlinx.serialization.Serializable

class CardsExportDialogState(
    decks: List<Deck>,
    fileFormat: CardsFileFormat? = null,
    stage: Stage = Stage.WaitingForFileFormat
) : FlowMaker<CardsExportDialogState>() {
    val decks: List<Deck> by flowMaker(decks)
    var fileFormat: CardsFileFormat? by flowMaker(fileFormat)
    var stage: Stage by flowMaker(stage)
}

@Serializable
sealed class Stage {
    @Serializable
    object WaitingForFileFormat : Stage()
    @Serializable
    object WaitingForDestination : Stage()
    @Serializable
    object Exporting : Stage()
    @Serializable
    class Finished(
        val exportedDeckNames: List<String>,
        val failedDeckNames: List<String>
    ) : Stage()
}