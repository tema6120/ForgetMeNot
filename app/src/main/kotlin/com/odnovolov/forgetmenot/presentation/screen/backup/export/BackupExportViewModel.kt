package com.odnovolov.forgetmenot.presentation.screen.backup.export

import kotlinx.coroutines.flow.Flow

class BackupExportViewModel(state: BackupExportDialogState) {
    val stage: Flow<BackupExportDialogState.Stage> = state.flowOf(BackupExportDialogState::stage)
}