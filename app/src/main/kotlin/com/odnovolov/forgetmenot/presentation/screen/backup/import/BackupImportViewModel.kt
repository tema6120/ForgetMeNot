package com.odnovolov.forgetmenot.presentation.screen.backup.import

import kotlinx.coroutines.flow.Flow

class BackupImportViewModel(state: BackupImportDialogState) {
    val stage: Flow<BackupImportDialogState.Stage> = state.flowOf(BackupImportDialogState::stage)
}