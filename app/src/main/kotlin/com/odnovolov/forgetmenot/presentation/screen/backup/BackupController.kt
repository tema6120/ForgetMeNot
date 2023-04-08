package com.odnovolov.forgetmenot.presentation.screen.backup

import com.odnovolov.forgetmenot.persistence.backup.Backupper
import com.odnovolov.forgetmenot.persistence.backup.Backupper.Result
import com.odnovolov.forgetmenot.persistence.backup.Backupper.Result.Success
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.backup.BackupController.Command
import com.odnovolov.forgetmenot.presentation.screen.backup.BackupController.Command.ShowImportResultAndRestartApp
import com.odnovolov.forgetmenot.presentation.screen.backup.BackupEvent.ExportButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.backup.BackupEvent.ReadyToImportBackup
import com.odnovolov.forgetmenot.presentation.screen.backup.export.BackupExportDiScope

class BackupController(
    private val backupper: Backupper,
    private val navigator: Navigator
) : BaseController<BackupEvent, Command>() {
    sealed class Command {
        class ShowImportResultAndRestartApp(val success: Boolean) : Command()
    }

    override val autoSave: Boolean = false

    override fun handle(event: BackupEvent) {
        when (event) {
            // todo: Move to BackupImportController
            is ReadyToImportBackup -> {
                val result: Result = backupper.import(event.inputStream)
                val success: Boolean = result == Success
                sendCommand(ShowImportResultAndRestartApp(success))
            }

            ExportButtonClicked -> {
                navigator.showBackupExportDialog(::BackupExportDiScope)
            }
        }
    }

    override fun saveState() {}
}