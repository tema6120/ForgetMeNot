package com.odnovolov.forgetmenot.presentation.screen.backup

sealed class BackupEvent {
    object ImportButtonClicked : BackupEvent()
    object ExportButtonClicked : BackupEvent()
}
