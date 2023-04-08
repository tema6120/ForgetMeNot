package com.odnovolov.forgetmenot.presentation.screen.backup.export

import java.io.OutputStream

sealed class BackupExportEvent {
    class ReadyToExportBackup(
        val outputStream: OutputStream,
        val fileName: String
    ) : BackupExportEvent()
}