package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.common.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.adddeck.AddDeckInteractor
import com.odnovolov.forgetmenot.domain.interactor.adddeck.Stage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.ext.getOrCreateScope

class AddDeckViewModel(
    private val globalState: GlobalState
) : ViewModel() {
    private val koinScope = getOrCreateScope()
    private val addDeckInteractorState: AddDeckInteractor.State = koinScope.get()
    private val addDeckScreenState: AddDeckScreenState = koinScope.get()
    val controller: AddDeckController = koinScope.get()

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
        koinScope.close()
    }
}