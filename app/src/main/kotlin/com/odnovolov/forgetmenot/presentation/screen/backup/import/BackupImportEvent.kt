package com.odnovolov.forgetmenot.presentation.screen.backup.import

import java.io.InputStream

sealed class BackupImportEvent {
    class ReadyToImportBackup(val inputStream: InputStream) : BackupImportEvent()
}