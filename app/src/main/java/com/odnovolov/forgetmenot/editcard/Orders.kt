package com.odnovolov.forgetmenot.editcard

sealed class EditCardOrder {
    object UpdateQuestionAndAnswer : EditCardOrder()
    object NavigateUp : EditCardOrder()
}