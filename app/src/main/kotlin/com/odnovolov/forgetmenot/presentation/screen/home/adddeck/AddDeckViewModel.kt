package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.interactor.deckadder.DeckAdder
import com.odnovolov.forgetmenot.domain.interactor.deckadder.Stage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AddDeckViewModel(
    deckAdderState: DeckAdder.State,
    addDeckScreenState: AddDeckScreenState,
    private val globalState: GlobalState
) {
    private val stage: Flow<Stage> = deckAdderState.flowOf(DeckAdder.State::stage)

    val isProcessing: Flow<Boolean> = stage.map { it == Stage.Parsing }

    val isDialogVisible: Flow<Boolean> = stage.map { it === Stage.WaitingForName }

    val nameCheckResult: Flow<NameCheckResult> = addDeckScreenState
        .flowOf(AddDeckScreenState::typedText)
        .map { typedText ->
            when {
                typedText.isEmpty() -> NameCheckResult.Empty
                isDeckNameOccupied(typedText) -> NameCheckResult.Occupied
                else -> NameCheckResult.Ok
            }
        }

    val isPositiveButtonEnabled: Flow<Boolean> = nameCheckResult.map { it == NameCheckResult.Ok }

    private fun isDeckNameOccupied(testedName: String): Boolean {
        return globalState.decks.any { it.name == testedName }
    }
}