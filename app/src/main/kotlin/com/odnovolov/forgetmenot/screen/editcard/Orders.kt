package com.odnovolov.forgetmenot.screen.editcard

sealed class EditCardOrder {
    object UpdateQuestionAndAnswer : EditCardOrder()
    object NavigateUp : EditCardOrder()
}