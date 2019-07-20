package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import androidx.lifecycle.LiveData
import com.odnovolov.forgetmenot.presentation.common.ViewModel
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckViewModel.*
import java.io.InputStream

interface AddDeckViewModel : ViewModel<State, Action, Event> {

    data class State(
        val isProcessing: LiveData<Boolean>,
        val isDialogVisible: LiveData<Boolean>,
        val errorText: LiveData<String?>,
        val isPositiveButtonEnabled: LiveData<Boolean>
    )

    sealed class Action {
        object ShowFileChooser : Action()
        data class ShowToast(val text: String) : Action()
        data class SetDialogText(val text: String) : Action()
    }

    sealed class Event {
        object AddButtonClicked : Event()
        data class ContentReceived(val inputStream: InputStream, val fileName: String?) : Event()
        data class DialogTextChanged(val text: String) : Event()
        object PositiveDialogButtonClicked : Event()
        object NegativeDialogButtonClicked : Event()
    }

}