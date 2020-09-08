package com.odnovolov.forgetmenot.presentation.screen.pronunciationplan

import com.odnovolov.forgetmenot.domain.entity.PronunciationEvent

data class PronunciationEventItem(
    val pronunciationEvent: PronunciationEvent,
    val isRemovable: Boolean
)