package com.odnovolov.forgetmenot.presentation.screen.exercise

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseScreenState(
    var wereDeckSettingsEdited: Boolean = false
)