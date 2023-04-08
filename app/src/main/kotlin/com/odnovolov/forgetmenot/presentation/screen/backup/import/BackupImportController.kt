package com.odnovolov.forgetmenot.presentation.screen.backup.import

import com.odnovolov.forgetmenot.persistence.backup.Backupper
import com.odnovolov.forgetmenot.persistence.backup.Backupper.Result
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.backup.import.BackupImportDialogState.Stage.Finished
import com.odnovolov.forgetmenot.presentation.screen.backup.import.BackupImportDialogState.Stage.InProgress
import com.odnovolov.forgetmenot.presentation.screen.backup.import.BackupImportEvent.ReadyToImportBackup

class BackupImportController(
    private val backupper: Backupper,
    private val state: BackupImportDialogState
) : BaseController<BackupImportEvent, Nothing>() {
    override val autoSave: Boolean = false

    override fun handle(event: BackupImportEvent) {
        when (event) {
            is ReadyToImportBackup -> {
                state.stage = InProgress
                val result: Result = backupper.import(event.inputStream)
                state.stage = Finished(result)
            }
        }
    }

    override fun saveState() {}
}