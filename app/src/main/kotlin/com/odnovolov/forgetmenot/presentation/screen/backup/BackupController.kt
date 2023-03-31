package com.odnovolov.forgetmenot.presentation.screen.backup

import com.odnovolov.forgetmenot.persistence.backup.Backupper
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.backup.BackupEvent.ExportButtonClicked

class BackupController(
    private val backupper: Backupper,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<BackupEvent, Nothing>() {
    override fun handle(event: BackupEvent) {
        when (event) {
            is ExportButtonClicked -> {
                backupper.export(event.outputStream)
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}