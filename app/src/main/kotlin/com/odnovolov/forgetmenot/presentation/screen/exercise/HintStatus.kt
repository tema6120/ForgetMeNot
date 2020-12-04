package com.odnovolov.forgetmenot.presentation.screen.exercise

sealed class HintStatus {
    object Off : HintStatus()
    object NotAccessibleBecauseCardIsAnswered : HintStatus()
    object NotAccessibleBecauseCardIsLearned : HintStatus()
    data class Accessible(
        val isGettingVariantsAccessible: Boolean,
        val currentMaskingLettersAction: MaskingLettersAction
    ) : HintStatus()

    enum class MaskingLettersAction {
        MaskLetters,
        UnmaskTheFirstLetter,
        UnmaskSelectedRegion
    }
}