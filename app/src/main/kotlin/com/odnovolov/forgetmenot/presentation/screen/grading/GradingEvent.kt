package com.odnovolov.forgetmenot.presentation.screen.grading

sealed class GradingEvent {
    object FirstCorrectAnswerButton : GradingEvent()
    object FirstWrongAnswerButton : GradingEvent()
    object YesAskAgainButton : GradingEvent()
    object NoAskAgainButton : GradingEvent()
    object RepeatedCorrectAnswerButton : GradingEvent()
    object RepeatedWrongAnswerButton : GradingEvent()
}