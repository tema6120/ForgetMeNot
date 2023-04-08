package com.odnovolov.forgetmenot.presentation.screen.backup

import java.io.InputStream

sealed class BackupEvent {
    class ReadyToImportBackup(val inputStream: InputStream) : BackupEvent()
    object ExportButtonClicked : BackupEvent()
}
