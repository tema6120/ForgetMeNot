package com.odnovolov.forgetmenot.presentation.screen.home.addcards

import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.checkDeckName
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class AddCardsViewModel(
    screenState: AddCardsScreenState,
    private val globalState: GlobalState
) {
    val isDialogVisible: Flow<Boolean> = screenState.flowOf(AddCardsScreenState::isDeckBeingCreated)
        .flowOn(businessLogicThread)

    val nameCheckResult: Flow<NameCheckResult> = screenState
        .flowOf(AddCardsScreenState::typedText)
        .map { typedText -> checkDeckName(typedText, globalState) }
        .flowOn(businessLogicThread)
}