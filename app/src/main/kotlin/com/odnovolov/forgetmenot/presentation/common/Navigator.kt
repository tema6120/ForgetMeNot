package com.odnovolov.forgetmenot.presentation.common

interface Navigator {
    fun navigateToExercise()
    fun navigateToEditCard()
    fun navigateToDeckSettings()
    fun navigateToIntervals()
    fun navigateToPronunciation()
    fun navigateToRepetitionSettings()
    fun showLastAnswerFilterDialog()
    fun navigateToRepetition()
    fun navigateToSettings()
    fun navigateToWalkingModeSettings()
    fun navigateUp()
}