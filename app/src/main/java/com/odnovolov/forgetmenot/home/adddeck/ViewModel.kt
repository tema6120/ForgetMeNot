package com.odnovolov.forgetmenot.home.adddeck

import com.odnovolov.forgetmenot.common.database.asFlow
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.mapToOne
import com.odnovolov.forgetmenot.common.database.stageAdapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AddDeckViewModel {
    private val queries: AddDeckViewModelQueries = database.addDeckViewModelQueries

    private val stage: Flow<Stage> = queries
        .getStage()
        .asFlow()
        .mapToOne()
        .map { databaseValue: String -> stageAdapter.decode(databaseValue) }

    val isProcessing: Flow<Boolean> = stage.map { it === Stage.Parsing }

    val isDialogVisible: Flow<Boolean> = stage.map { it === Stage.WaitingForName }

    val errorText: Flow<String?> = queries
        .getErrorText()
        .asFlow()
        .mapToOne()
        .map { it.errorText }

    val isPositiveButtonEnabled: Flow<Boolean> = errorText.map { it == null }
}