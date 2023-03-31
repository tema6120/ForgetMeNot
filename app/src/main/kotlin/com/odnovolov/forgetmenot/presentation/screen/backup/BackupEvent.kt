package com.odnovolov.forgetmenot.presentation.screen.backup

import java.io.OutputStream

sealed class BackupEvent {
    class ExportButtonClicked(val outputStream: OutputStream) : BackupEvent()
}
