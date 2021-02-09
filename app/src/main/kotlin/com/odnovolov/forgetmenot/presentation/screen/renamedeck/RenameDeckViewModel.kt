package com.odnovolov.forgetmenot.presentation.screen.renamedeck

import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.checkDeckName
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class RenameDeckViewModel(
    private val dialogState: RenameDeckDialogState,
    private val globalState: GlobalState
) {
    val deckName: String get() = dialogState.typedDeckName

    val deckNameCheckResult: Flow<NameCheckResult> =
        dialogState.flowOf(RenameDeckDialogState::typedDeckName)
            .map { typedDeckName: String -> checkDeckName(typedDeckName, globalState) }
            .flowOn(businessLogicThread)
}