package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.entity.checkDeckName
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.DeckFromFileCreator
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.DeckFromFileCreator.State.Stage
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenState.HowToAdd
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class AddDeckViewModel(
    deckAdderState: DeckFromFileCreator.State,
    screenState: AddDeckScreenState,
    private val globalState: GlobalState
) {
    private val stage: Flow<Stage> = deckAdderState.flowOf(DeckFromFileCreator.State::stage)

    val isProcessing: Flow<Boolean> = stage.map { it == Stage.Parsing }
        .flowOn(businessLogicThread)

    val isDialogVisible: Flow<Boolean> = combine(
        stage,
        screenState.flowOf(AddDeckScreenState::howToAdd)
    ) { stage: Stage, howToAdd: HowToAdd? ->
        stage == Stage.WaitingForName || howToAdd == HowToAdd.CREATE
    }
        .flowOn(businessLogicThread)

    val nameCheckResult: Flow<NameCheckResult> = screenState
        .flowOf(AddDeckScreenState::typedText)
        .map { typedText -> checkDeckName(typedText, globalState) }
        .flowOn(businessLogicThread)
}