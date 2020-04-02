package com.odnovolov.forgetmenot.presentation.screen.speakplan

import com.odnovolov.forgetmenot.domain.entity.SpeakEvent

data class SpeakEventItem(
    val speakEvent: SpeakEvent,
    val isRemovable: Boolean
)