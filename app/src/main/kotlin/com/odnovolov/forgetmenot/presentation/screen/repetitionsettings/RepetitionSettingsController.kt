package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings

import LAST_ANSWER_FILTER_SCOPE_ID
import REPETITION_LAPS_SCOPE_ID
import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.checkRepetitionSettingName
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.entity.RepetitionSetting
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionSettings
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionStateCreator
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionStateCreator.NoCardIsReadyForRepetition
import com.odnovolov.forgetmenot.domain.toDateTimeSpan
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.entity.DisplayedInterval
import com.odnovolov.forgetmenot.presentation.common.entity.NamePresetDialogStatus.*
import com.odnovolov.forgetmenot.presentation.screen.repetition.REPETITION_SCOPE_ID
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.RepetitionSettingsController.Command.SetNamePresetDialogText
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.RepetitionSettingsController.Command.ShowNoCardIsReadyForRepetitionMessage
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps.RepetitionLapsDialogState
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps.RepetitionLapsViewModel
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.lastanswer.LastAnswerFilterDialogState
import com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.lastanswer.LastAnswerFilterViewModel
import com.soywiz.klock.DateTimeSpan
import com.soywiz.klock.days
import kotlinx.coroutines.flow.Flow
import org.koin.java.KoinJavaComponent.getKoin

class RepetitionSettingsController(
    private val repetitionSettings: RepetitionSettings,
    private val repetitionStateCreator: RepetitionStateCreator,
    private val screenState: RepetitionSettingsScreenState,
    private val globalState: GlobalState,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val repetitionCreatorStateProvider: ShortTermStateProvider<RepetitionStateCreator.State>,
    private val screenStateProvider: ShortTermStateProvider<RepetitionSettingsScreenState>
) {
    sealed class Command {
        object ShowNoCardIsReadyForRepetitionMessage : Command()
        class SetNamePresetDialogText(val text: String) : Command()
    }

    private val commandFlow = EventFlow<Command>()
    val commands: Flow<Command> = commandFlow.get()

    fun onStartRepetitionMenuItemClicked() {
        val repetitionState: Repetition.State = try {
            repetitionStateCreator.create()
        } catch (e: NoCardIsReadyForRepetition) {
            commandFlow.send(ShowNoCardIsReadyForRepetitionMessage)
            return
        }
        longTermStateSaver.saveStateByRegistry()
        val koinScope = getKoin().createScope<Repetition>(REPETITION_SCOPE_ID)
        koinScope.declare(repetitionState, override = true)
        navigator.navigateToRepetition()
    }

    fun onSavePresetButtonClicked() {
        screenState.namePresetDialogStatus = VisibleToMakeIndividualPresetAsShared
        commandFlow.send(SetNamePresetDialogText(""))
    }

    fun onSetRepetitionSettingsClicked(repetitionSettingsId: Long) {
        repetitionSettings.setCurrentRepetitionSetting(repetitionSettingsId)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onRenameRepetitionSettingsClicked(repetitionSettingsId: Long) {
        screenState.renamePresetId = repetitionSettingsId
        screenState.namePresetDialogStatus = VisibleToRenameSharedPreset
        globalState.sharedRepetitionSettings.find { it.id == repetitionSettingsId }
            ?.name
            ?.let { repetitionSettingName: String ->
                commandFlow.send(SetNamePresetDialogText(repetitionSettingName))
            }
    }

    fun onDeleteRepetitionSettingsClicked(repetitionSettingsId: Long) {
        repetitionSettings.deleteSharedRepetitionSetting(repetitionSettingsId)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onAddNewRepetitionSettingsClicked() {
        screenState.namePresetDialogStatus = VisibleToCreateNewSharedPreset
        commandFlow.send(SetNamePresetDialogText(""))
    }

    fun onDialogTextChanged(text: String) {
        screenState.typedPresetName = text
    }

    fun onNamePresetPositiveDialogButtonClicked() {
        val newPresetName: String = screenState.typedPresetName
        if (checkRepetitionSettingName(newPresetName, globalState) != NameCheckResult.Ok) return
        when (screenState.namePresetDialogStatus) {
            VisibleToMakeIndividualPresetAsShared -> {
                val repetitionSetting = globalState.currentRepetitionSetting
                repetitionSettings.renameRepetitionSetting(repetitionSetting, newPresetName)
            }
            VisibleToCreateNewSharedPreset -> {
                repetitionSettings.createNewSharedRepetitionSetting(newPresetName)
            }
            VisibleToRenameSharedPreset -> {
                globalState.sharedRepetitionSettings
                    .find { it.id == screenState.renamePresetId }
                    ?.let { repetitionSetting: RepetitionSetting ->
                        repetitionSettings.renameRepetitionSetting(repetitionSetting, newPresetName)
                    }
            }
            Invisible -> {
            }
        }
        screenState.namePresetDialogStatus = Invisible
        longTermStateSaver.saveStateByRegistry()
    }

    fun onNamePresetNegativeDialogButtonClicked() {
        screenState.namePresetDialogStatus = Invisible
    }

    fun onAvailableForExerciseGroupButtonClicked() {
        repetitionSettings.setIsAvailableForExerciseCardsIncluded(
            !globalState.currentRepetitionSetting.isAvailableForExerciseCardsIncluded
        )
        longTermStateSaver.saveStateByRegistry()
    }

    fun onAwaitingGroupButtonClicked() {
        repetitionSettings.setIsAwaitingCardsIncluded(
            !globalState.currentRepetitionSetting.isAwaitingCardsIncluded
        )
        longTermStateSaver.saveStateByRegistry()
    }

    fun onLearnedGroupButtonClicked() {
        repetitionSettings.setIsLearnedCardsIncluded(
            !globalState.currentRepetitionSetting.isLearnedCardsIncluded
        )
        longTermStateSaver.saveStateByRegistry()
    }

    fun onLevelOfKnowledgeRangeChanged(levelOfKnowledgeRange: IntRange) {
        repetitionSettings.setLevelOfKnowledgeRange(levelOfKnowledgeRange)
        longTermStateSaver.saveStateByRegistry()
    }

    fun onLastAnswerFromButtonClicked() {
        showLastAnswerFilterDialog(isFromDialog = true)
    }

    fun onLastAnswerToButtonClicked() {
        showLastAnswerFilterDialog(isFromDialog = false)
    }

    private fun showLastAnswerFilterDialog(isFromDialog: Boolean) {
        val dateTimeSpan: DateTimeSpan? =
            if (isFromDialog) globalState.currentRepetitionSetting.lastAnswerFromTimeAgo
            else globalState.currentRepetitionSetting.lastAnswerToTimeAgo
        val dialogState = LastAnswerFilterDialogState(
            isFromDialog = isFromDialog,
            isZeroTimeSelected = dateTimeSpan == null,
            timeAgo = dateTimeSpan?.let(DisplayedInterval.Companion::fromDateTimeSpan)
                ?: DisplayedInterval.fromDateTimeSpan(7.days.toDateTimeSpan())
        )
        val koinScope =
            getKoin().createScope<LastAnswerFilterViewModel>(LAST_ANSWER_FILTER_SCOPE_ID)
        koinScope.declare(dialogState, override = true)
        navigator.showLastAnswerFilterDialog()
    }

    fun onLapsButtonClicked() {
        val isInfinite = globalState.currentRepetitionSetting.numberOfLaps == Int.MAX_VALUE
        val numberOfLapsInput: String =
            if (isInfinite) "1"
            else globalState.currentRepetitionSetting.numberOfLaps.toString()
        val dialogState = RepetitionLapsDialogState(
            isInfinitely = isInfinite,
            numberOfLapsInput = numberOfLapsInput
        )
        val koinScope = getKoin().createScope<RepetitionLapsViewModel>(REPETITION_LAPS_SCOPE_ID)
        koinScope.declare(dialogState, override = true)
        navigator.showRepetitionLapsDialog()
    }

    fun onFragmentPause() {
        repetitionCreatorStateProvider.save(repetitionStateCreator.state)
        screenStateProvider.save(screenState)
    }
}