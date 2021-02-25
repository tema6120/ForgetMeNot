package com.odnovolov.forgetmenot.presentation.screen.export

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileFormat
import kotlinx.serialization.Serializable

class ExportDialogState(
    decks: List<Deck>,
    fileFormat: FileFormat? = null,
    stage: Stage = Stage.WaitingForFileFormat
) : FlowMaker<ExportDialogState>() {
    val decks: List<Deck> by flowMaker(decks)
    var fileFormat: FileFormat? by flowMaker(fileFormat)
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