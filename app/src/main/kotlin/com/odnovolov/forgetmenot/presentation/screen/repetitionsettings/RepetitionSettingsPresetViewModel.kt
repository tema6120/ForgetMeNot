package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings

import com.odnovolov.forgetmenot.domain.checkRepetitionSettingName
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.entity.RepetitionSetting
import com.odnovolov.forgetmenot.presentation.common.customview.preset.Preset
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetDialogState
import com.odnovolov.forgetmenot.presentation.common.customview.preset.SkeletalPresetViewModel
import kotlinx.coroutines.flow.*

class RepetitionSettingsPresetViewModel(
    presetDialogState: PresetDialogState,
    private val globalState: GlobalState
) : SkeletalPresetViewModel() {
    override val availablePresets: Flow<List<Preset>> = combine(
        globalState.flowOf(GlobalState::currentRepetitionSetting),
        globalState.flowOf(GlobalState::sharedRepetitionSettings)
    ) { currentRepetitionSetting: RepetitionSetting,
        sharedRepetitionSettings: Collection<RepetitionSetting>
        ->
        (sharedRepetitionSettings + currentRepetitionSetting + RepetitionSetting.Default)
            .distinctBy { it.id }
    }
        .flatMapLatest { repetitionSettings: List<RepetitionSetting> ->
            val repetitionSettingNameFlows: List<Flow<String>> = repetitionSettings
                .map { it.flowOf(RepetitionSetting::name) }
            combine(repetitionSettingNameFlows) {
                repetitionSettings.map { repetitionSetting: RepetitionSetting ->
                    with(repetitionSetting) {
                        Preset(
                            id = id,
                            name = name,
                            isSelected = id == globalState.currentRepetitionSetting.id
                        )
                    }
                }
                    .sortedWith(compareBy({ it.name }, { it.id }))
            }
        }

    override val presetInputCheckResult: Flow<NameCheckResult> = presetDialogState
        .flowOf(PresetDialogState::typedPresetName)
        .map { typedPresetName: String -> checkRepetitionSettingName(typedPresetName, globalState) }

    override val deckNamesThatUsePreset: Flow<List<String>> = flowOf(emptyList())
}