package com.odnovolov.forgetmenot.presentation.screen.backup

import com.odnovolov.forgetmenot.persistence.backup.Backupper
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class BackupDiScope {
    private val backupper = Backupper()

    val controller = BackupController(
        backupper
    )

    val viewModel = BackupViewModel(

    )

    companion object : DiScopeManager<BackupDiScope>() {
        override fun recreateDiScope() = BackupDiScope()

        override fun onCloseDiScope(diScope: BackupDiScope) {
            diScope.controller.dispose()
        }
    }
}