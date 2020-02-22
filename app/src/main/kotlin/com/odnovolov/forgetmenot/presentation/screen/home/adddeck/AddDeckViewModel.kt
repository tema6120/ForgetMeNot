package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.adddeck.AddDeckInteractor
import com.odnovolov.forgetmenot.domain.interactor.adddeck.Stage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.KoinComponent

class AddDeckViewModel(
    addDeckInteractorState: AddDeckInteractor.State,
    addDeckScreenState: AddDeckScreenState,
    private val globalState: GlobalState
) : ViewModel(), KoinComponent {

    private val stage: Flow<Stage> = addDeckInteractorState.flowOf(AddDeckInteractor.State::stage)

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

    override fun onCleared() {
        getKoin().getScope(ADD_DECK_SCOPE_ID).close()
    }
}