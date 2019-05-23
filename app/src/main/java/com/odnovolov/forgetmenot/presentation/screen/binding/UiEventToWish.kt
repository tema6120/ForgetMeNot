package com.odnovolov.forgetmenot.presentation.screen.binding

import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature.Wish
import com.odnovolov.forgetmenot.presentation.screen.HomeFragment.UiEvent
import com.odnovolov.forgetmenot.presentation.screen.HomeFragment.UiEvent.*

class UiEventToWish : (UiEvent) -> Wish? {
    override fun invoke(uiEvent: UiEvent): Wish? {
        return when (uiEvent) {
            is GotData -> {
                if (uiEvent.fileName == null) {
                    Wish.AddFromInputStream(uiEvent.inputStream)
                } else {
                    Wish.AddFromInputStream(uiEvent.inputStream, fileName = uiEvent.fileName)
                }
            }
            is SubmitRenameDialogText -> Wish.OfferName(uiEvent.dialogText)
            is CancelRenameDialog -> Wish.Cancel
        }
    }
}