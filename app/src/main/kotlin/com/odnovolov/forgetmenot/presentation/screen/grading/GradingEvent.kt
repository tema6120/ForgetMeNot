package com.odnovolov.forgetmenot.presentation.screen.grading

import com.odnovolov.forgetmenot.domain.entity.GradeChange

sealed class GradingEvent {
    object HelpButtonClicked : GradingEvent()
    object CloseTipButtonClicked : GradingEvent()
    object FirstCorrectAnswerButtonClicked : GradingEvent()
    object FirstWrongAnswerButtonClicked : GradingEvent()
    object YesAskAgainButtonClicked : GradingEvent()
    object NoAskAgainButtonClicked : GradingEvent()
    object RepeatedCorrectAnswerButtonClicked : GradingEvent()
    object RepeatedWrongAnswerButtonClicked : GradingEvent()
    class GradeChangeWasSelected(val gradeChange: GradeChange) : GradingEvent()
}