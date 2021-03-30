package com.odnovolov.forgetmenot.presentation.common.customview.undoredoedittext

import kotlinx.serialization.Serializable

@Serializable
data class TextChange(
    var newText: String,
    var oldText: String,
    var start: Int
)