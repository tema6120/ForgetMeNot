package com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.preset

import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

abstract class SkeletalPresetViewModel {
    abstract val availablePresets: Flow<List<Preset>>

    val currentPreset: Flow<Preset>
        get() = availablePresets.map { presets: List<Preset> -> presets.first { it.isSelected } }

    abstract val presetInputCheckResult: Flow<NameCheckResult>

    abstract val deckNamesThatUsePreset: Flow<List<String>>
}