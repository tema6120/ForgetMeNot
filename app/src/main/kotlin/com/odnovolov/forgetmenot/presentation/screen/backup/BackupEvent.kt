package com.odnovolov.forgetmenot.presentation.screen.backup

sealed class BackupEvent {
    object HelpButtonClicked : BackupEvent()
    object ImportButtonClicked : BackupEvent()
    object ExportButtonClicked : BackupEvent()
}
