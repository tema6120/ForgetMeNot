package com.odnovolov.forgetmenot.presentation.screen.editcard

sealed class EditCardCommand {
    object UpdateQuestionAndAnswer : EditCardCommand()
}