package com.odnovolov.forgetmenot.presentation.screen.backup.export

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import kotlin.Exception

class BackupExportDialogState : FlowMaker<BackupExportDialogState>() {
    var stage: Stage by flowMaker(Stage.WaitingForDestination)

    sealed class Stage {
        object WaitingForDestination : Stage()
        object InProgress : Stage()
        class Finished(val result: ExportResult) : Stage()
    }

    sealed class ExportResult {
        class Success(val fileName: String) : ExportResult()
        class Failure(val exception: Exception) : ExportResult()
    }
}