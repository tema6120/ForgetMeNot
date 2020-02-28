package com.odnovolov.forgetmenot.presentation.screen.intervals

import com.odnovolov.forgetmenot.common.entity.NamePresetDialogStatus.*
import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.checkIntervalSchemeName
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.domain.entity.IntervalScheme
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.interactor.decksettings.IntervalsSettings
import com.odnovolov.forgetmenot.presentation.common.Store
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsCommand.SetNamePresetDialogText
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsCommand.ShowModifyIntervalDialog
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.MODIFY_INTERVAL_SCOPE_ID
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.ModifyIntervalDialogState
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.ModifyIntervalViewModel
import com.soywiz.klock.DateTimeSpan
import kotlinx.coroutines.flow.Flow
import org.koin.core.KoinComponent

class IntervalsController(
    private val deckSettingsState: DeckSettings.State,
    private val intervalsSettings: IntervalsSettings,
    private val intervalsScreenState: IntervalsScreenState,
    private val globalState: GlobalState,
    private val store: Store
) : KoinComponent {
    private var isFragmentRemoving = false
    private val commandFlow = EventFlow<IntervalsCommand>()
    val commands: Flow<IntervalsCommand> = commandFlow.get()

    fun onSaveIntervalSchemeButtonClicked() {
        intervalsScreenState.namePresetDialogStatus = VisibleToMakeIndividualPresetAsShared
        commandFlow.send(SetNamePresetDialogText(""))
    }

    fun onSetIntervalSchemeButtonClicked(intervalSchemeId: Long?) {
        intervalsSettings.setIntervalScheme(intervalSchemeId)
        store.saveStateByRegistry()
    }

    fun onRenameIntervalSchemeButtonClicked(intervalSchemeId: Long) {
        intervalsScreenState.renamePresetId = intervalSchemeId
        intervalsScreenState.namePresetDialogStatus = VisibleToRenameSharedPreset
        globalState.sharedIntervalSchemes.find { it.id == intervalSchemeId }
            ?.name
            ?.let { intervalSchemeName: String ->
                commandFlow.send(SetNamePresetDialogText(intervalSchemeName))
            }
    }

    fun onDeleteIntervalSchemeButtonClicked(intervalSchemeId: Long) {
        intervalsSettings.deleteSharedIntervalScheme(intervalSchemeId)
        store.saveStateByRegistry()
    }

    fun onAddNewIntervalSchemeButtonClicked() {
        intervalsScreenState.namePresetDialogStatus = VisibleToCreateNewSharedPreset
        commandFlow.send(SetNamePresetDialogText(""))
    }

    fun onDialogTextChanged(text: String) {
        intervalsScreenState.typedPresetName = text
    }

    fun onNamePresetPositiveDialogButtonClicked() {
        val newPresetName: String = intervalsScreenState.typedPresetName
        if (checkIntervalSchemeName(newPresetName, globalState) != NameCheckResult.Ok) return
        when (intervalsScreenState.namePresetDialogStatus) {
            VisibleToMakeIndividualPresetAsShared -> {
                val intervalScheme = deckSettingsState.deck.exercisePreference.intervalScheme
                    ?: return
                intervalsSettings.renameIntervalScheme(intervalScheme, newPresetName)
            }
            VisibleToCreateNewSharedPreset -> {
                intervalsSettings.createNewSharedIntervalScheme(newPresetName)
            }
            VisibleToRenameSharedPreset -> {
                globalState.sharedIntervalSchemes
                    .find { it.id == intervalsScreenState.renamePresetId }
                    ?.let { intervalScheme: IntervalScheme ->
                        intervalsSettings.renameIntervalScheme(
                            intervalScheme,
                            newPresetName
                        )
                    }
            }
            Invisible -> {
            }
        }
        intervalsScreenState.namePresetDialogStatus = Invisible
        store.saveStateByRegistry()
    }

    fun onNamePresetNegativeDialogButtonClicked() {
        intervalsScreenState.namePresetDialogStatus = Invisible
    }

    fun onModifyIntervalButtonClicked(targetLevelOfKnowledge: Int) {
        val interval: Interval = deckSettingsState.deck.exercisePreference.intervalScheme
            ?.intervals?.find { it.targetLevelOfKnowledge == targetLevelOfKnowledge } ?: return
        val modifyIntervalDialogState = ModifyIntervalDialogState(
            targetLevelOfKnowledge = targetLevelOfKnowledge,
            displayedInterval = DisplayedInterval.fromDateTimeSpan(interval.value)
        )
        val koinScope = getKoin().createScope<ModifyIntervalViewModel>(MODIFY_INTERVAL_SCOPE_ID)
        koinScope.declare(modifyIntervalDialogState, override = true)
        commandFlow.send(ShowModifyIntervalDialog)
    }

    fun onAddIntervalButtonClicked() {
        val lastIntervalValue: DateTimeSpan = deckSettingsState.deck.exercisePreference
            .intervalScheme?.intervals?.last()?.value ?: return
        intervalsSettings.addInterval(lastIntervalValue)
        store.saveStateByRegistry()
    }

    fun onRemoveIntervalButtonClicked() {
        intervalsSettings.removeLastInterval()
        store.saveStateByRegistry()
    }

    fun onFragmentRemoving() {
        isFragmentRemoving = true
    }

    fun onCleared() {
        if (isFragmentRemoving) {
            store.deleteIntervalsScreenState()
        } else {
            store.save(intervalsScreenState)
        }
    }
}