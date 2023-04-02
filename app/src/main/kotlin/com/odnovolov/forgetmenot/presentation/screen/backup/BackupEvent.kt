package com.odnovolov.forgetmenot.presentation.screen.backup

import java.io.InputStream
import java.io.OutputStream

sealed class BackupEvent {
    class ReadyToExportBackup(val outputStream: OutputStream) : BackupEvent()
    class ReadyToImportBackup(val inputStream: InputStream) : BackupEvent()
}
