package com.odnovolov.forgetmenot.presentation.screen.backup

import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.backup.BackupEvent.ExportButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.backup.BackupEvent.ImportButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.backup.export.BackupExportDiScope
import com.odnovolov.forgetmenot.presentation.screen.backup.import.BackupImportDiScope

class BackupController(
    private val navigator: Navigator
) : BaseController<BackupEvent, Nothing>() {
    override val autoSave: Boolean = false

    override fun handle(event: BackupEvent) {
        when (event) {
            ImportButtonClicked -> {
                navigator.showBackupImportDialog(::BackupImportDiScope)
            }

            ExportButtonClicked -> {
                navigator.showBackupExportDialog(::BackupExportDiScope)
            }
        }
    }

    override fun saveState() {}
}