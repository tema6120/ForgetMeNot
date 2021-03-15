package com.odnovolov.forgetmenot.presentation.screen.home.choosepreset

import com.odnovolov.forgetmenot.domain.entity.ExercisePreference

data class SelectablePreset(
    val exercisePreference: ExercisePreference,
    val isSelected: Boolean
)