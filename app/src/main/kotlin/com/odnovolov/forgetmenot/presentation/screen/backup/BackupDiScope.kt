package com.odnovolov.forgetmenot.presentation.screen.backup

import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager

class BackupDiScope {
    val controller = BackupController(
        AppDiScope.get().navigator
    )

    companion object : DiScopeManager<BackupDiScope>() {
        override fun recreateDiScope() = BackupDiScope()

        override fun onCloseDiScope(diScope: BackupDiScope) {
            diScope.controller.dispose()
        }
    }
}