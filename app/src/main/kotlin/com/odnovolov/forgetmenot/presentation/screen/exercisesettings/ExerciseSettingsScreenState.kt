package com.odnovolov.forgetmenot.presentation.screen.exercisesettings

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker

class ExerciseSettingsScreenState(
    cardsThresholdForFilterDialogInput: String
) : FlowMaker<ExerciseSettingsScreenState>() {
    var cardsThresholdForFilterDialogInput: String by flowMaker(cardsThresholdForFilterDialogInput)
}