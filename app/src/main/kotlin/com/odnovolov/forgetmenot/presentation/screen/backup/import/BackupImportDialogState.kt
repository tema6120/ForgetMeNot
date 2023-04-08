package com.odnovolov.forgetmenot.presentation.screen.backup.import

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.persistence.backup.Backupper

class BackupImportDialogState : FlowMaker<BackupImportDialogState>() {
    var stage: Stage by flowMaker(Stage.WaitingForBackupFile)

    sealed class Stage {
        object WaitingForBackupFile : Stage()
        object InProgress : Stage()
        class Finished(val result: Backupper.Result) : Stage()
    }
}