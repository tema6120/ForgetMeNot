package com.odnovolov.forgetmenot.home.adddeck

sealed class AddDeckOrder {
    class ShowErrorMessage(val text: String) : AddDeckOrder()
    class SetDialogText(val text: String) : AddDeckOrder()
}