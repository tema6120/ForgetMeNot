package com.odnovolov.forgetmenot.presentation.screen.grading

import com.odnovolov.forgetmenot.domain.entity.GradeChange

sealed class GradingEvent {
    object FirstCorrectAnswerButton : GradingEvent()
    object FirstWrongAnswerButton : GradingEvent()
    object YesAskAgainButton : GradingEvent()
    object NoAskAgainButton : GradingEvent()
    object RepeatedCorrectAnswerButton : GradingEvent()
    object RepeatedWrongAnswerButton : GradingEvent()
    class SelectedGradeChange(val gradeChange: GradeChange) : GradingEvent()
}