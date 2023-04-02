package com.odnovolov.forgetmenot.presentation.screen.backup

import com.odnovolov.forgetmenot.persistence.backup.Backupper
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.backup.BackupController.Command
import com.odnovolov.forgetmenot.presentation.screen.backup.BackupController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.backup.BackupEvent.ReadyToExportBackup
import com.odnovolov.forgetmenot.presentation.screen.backup.BackupEvent.ReadyToImportBackup

class BackupController(
    private val backupper: Backupper
) : BaseController<BackupEvent, Command>() {
    sealed class Command {
        class ShowImportResultAndRestartApp(val success: Boolean) : Command()
        class ShowExportResult(val success: Boolean) : Command()
    }

    override val autoSave: Boolean = false

    override fun handle(event: BackupEvent) {
        when (event) {
            is ReadyToImportBackup -> {
                val success: Boolean = backupper.import(event.inputStream)
                sendCommand(ShowImportResultAndRestartApp(success))
            }
            is ReadyToExportBackup -> {
                val success: Boolean = backupper.export(event.outputStream)
                sendCommand(ShowExportResult(success))
            }
        }
    }

    override fun saveState() {}
}