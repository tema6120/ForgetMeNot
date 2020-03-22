package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings

import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings.NoCardIsReadyForRepetition
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.UserSessionTermStateProvider
import com.odnovolov.forgetmenot.presentation.screen.repetition.REPETITION_SCOPE_ID
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.RepetitionSettingsController.Command.ShowNoCardIsReadyForRepetitionMessage
import kotlinx.coroutines.flow.Flow
import org.koin.java.KoinJavaComponent.getKoin

class RepetitionSettingsController(
    private val repetitionSettings: RepetitionSettings,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val repetitionSettingsStateProvider: UserSessionTermStateProvider<RepetitionSettings.State>
) {
    sealed class Command {
        object ShowNoCardIsReadyForRepetitionMessage : Command()
    }

    private val commandFlow = EventFlow<Command>()
    val commands: Flow<Command> = commandFlow.get()

    fun onAvailableForExerciseGroupButtonClicked() {
        with(repetitionSettings) {
            setIsAvailableForExerciseCardsIncluded(!state.isAvailableForExerciseCardsIncluded)
        }
        longTermStateSaver.saveStateByRegistry()
    }

    fun onAwaitingGroupButtonClicked() {
        with(repetitionSettings) {
            setIsAwaitingCardsIncluded(!state.isAwaitingCardsIncluded)
        }
        longTermStateSaver.saveStateByRegistry()
    }

    fun onLearnedGroupButtonClicked() {
        with(repetitionSettings) {
            setIsLearnedCardsIncluded(!state.isLearnedCardsIncluded)
        }
        longTermStateSaver.saveStateByRegistry()
    }

    fun onLevelOfKnowledgeRangeChanged(levelOfKnowledgeRange: IntRange) {
        repetitionSettings.setLevelOfKnowledgeRange(levelOfKnowledgeRange)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onStartRepetitionMenuItemClicked() {
        val repetitionState: Repetition.State = try {
            repetitionSettings.createRepetitionState()
        } catch (e: NoCardIsReadyForRepetition) {
            commandFlow.send(ShowNoCardIsReadyForRepetitionMessage)
            return
        }
        longTermStateSaver.saveStateByRegistry()
        val koinScope = getKoin().createScope<Repetition>(REPETITION_SCOPE_ID)
        koinScope.declare(repetitionState, override = true)
        navigator.navigateToRepetition()
    }

    fun onFragmentPause() {
        repetitionSettingsStateProvider.save(repetitionSettings.state)
    }
}