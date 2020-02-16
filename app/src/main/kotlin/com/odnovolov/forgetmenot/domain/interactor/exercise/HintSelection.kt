package com.odnovolov.forgetmenot.domain.interactor.exercise

import kotlinx.serialization.Serializable

@Serializable
data class HintSelection(
    var startIndex: Int,
    var endIndex: Int
)