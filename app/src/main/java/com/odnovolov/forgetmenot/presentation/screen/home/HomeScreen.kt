package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.feature.deckspreview.DeckPreview
import com.odnovolov.forgetmenot.presentation.common.Screen
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreen.*
import java.io.InputStream

class HomeScreen : Screen<ViewState, UiEvent, News>(
    initialViewState = ViewState()
) {
    data class ViewState(
        val decksPreview: List<DeckPreview> = emptyList(),
        val isRenameDialogVisible: Boolean = false,
        val isProcessing: Boolean = false
    )

    sealed class UiEvent {
        data class GotData(val inputStream: InputStream, val fileName: String?) : UiEvent()
        data class RenameDialogPositiveButtonClick(val dialogText: String) : UiEvent()
        object RenameDialogNegativeButtonClick : UiEvent()
        data class DeckButtonClick(val idx: Int) : UiEvent()
        data class DeleteDeckButtonClick(val idx: Int) : UiEvent()
    }

    sealed class News {
        object NavigateToExercise : News()
    }
}