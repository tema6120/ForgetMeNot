package com.odnovolov.forgetmenot.presentation.screen.player.view

import kotlinx.serialization.Serializable

@Serializable
data class PlayerScreenState(
    var wereDeckSettingsEdited: Boolean = false
)