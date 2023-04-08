package com.odnovolov.forgetmenot.presentation.screen.backup.import

import com.odnovolov.forgetmenot.persistence.backup.Backupper
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class BackupImportDiScope {
    private val backupper = Backupper()

    private val state = BackupImportDialogState()

    val controller = BackupImportController(
        backupper,
        state
    )

    val viewModel = BackupImportViewModel(
        state
    )

    companion object : DiScopeManager<BackupImportDiScope>() {
        override fun recreateDiScope() = BackupImportDiScope()

        override fun onCloseDiScope(diScope: BackupImportDiScope) {
            diScope.controller.dispose()
        }
    }
}