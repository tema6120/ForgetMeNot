package com.odnovolov.forgetmenot.presentation.screen.changegrade

import kotlinx.serialization.Serializable

@Serializable
data class ChangeGradeDialogState(
    val gradeItems: List<GradeItem>,
    val caller: ChangeGradeCaller
)

enum class ChangeGradeCaller {
    DeckEditor,
    Search
}