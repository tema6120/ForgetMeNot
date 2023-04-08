package com.odnovolov.forgetmenot.presentation.screen.backup.export

import com.odnovolov.forgetmenot.persistence.backup.Backupper
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class BackupExportDiScope {
    private val backupper = Backupper()

    private val state = BackupExportDialogState()

    val controller = BackupExportController(
        backupper,
        state
    )

    val viewModel = BackupExportViewModel(
        state
    )

    companion object : DiScopeManager<BackupExportDiScope>() {
        override fun recreateDiScope() = BackupExportDiScope()

        override fun onCloseDiScope(diScope: BackupExportDiScope) {
            diScope.controller.dispose()
        }
    }
}