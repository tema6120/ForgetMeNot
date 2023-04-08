package com.odnovolov.forgetmenot.presentation.screen.backup.export

import com.odnovolov.forgetmenot.persistence.backup.Backupper
import com.odnovolov.forgetmenot.persistence.backup.Backupper.Result
import com.odnovolov.forgetmenot.persistence.backup.Backupper.Result.Failure
import com.odnovolov.forgetmenot.persistence.backup.Backupper.Result.Success
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.backup.export.BackupExportEvent.ReadyToExportBackup
import com.odnovolov.forgetmenot.presentation.screen.backup.export.BackupExportDialogState.ExportResult
import com.odnovolov.forgetmenot.presentation.screen.backup.export.BackupExportDialogState.Stage.Finished
import com.odnovolov.forgetmenot.presentation.screen.backup.export.BackupExportDialogState.Stage.InProgress

class BackupExportController(
    private val backupper: Backupper,
    private val state: BackupExportDialogState
) : BaseController<BackupExportEvent, Nothing>() {
    override val autoSave: Boolean = false

    override fun handle(event: BackupExportEvent) {
        when (event) {
            is ReadyToExportBackup -> {
                state.stage = InProgress
                val result: Result = backupper.export(event.outputStream)
                val exportResult: ExportResult = when (result) {
                    Success -> ExportResult.Success(event.fileName)
                    is Failure -> ExportResult.Failure(result.exception)
                }
                state.stage = Finished(exportResult)
            }
        }
    }

    override fun saveState() {}
}