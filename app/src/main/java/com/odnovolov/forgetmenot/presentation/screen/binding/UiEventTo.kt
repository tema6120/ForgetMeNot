package com.odnovolov.forgetmenot.presentation.screen.binding

import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature
import com.odnovolov.forgetmenot.presentation.screen.HomeFragment.UiEvent
import com.odnovolov.forgetmenot.presentation.screen.HomeFragment.UiEvent.*

object UiEventTo {

    val addNewDeckFeatureWish: (UiEvent) -> AddNewDeckFeature.Wish? = { uiEvent: UiEvent ->
        when (uiEvent) {
            is GotData -> {
                if (uiEvent.fileName == null) {
                    AddNewDeckFeature.Wish.AddFromInputStream(uiEvent.inputStream)
                } else {
                    AddNewDeckFeature.Wish.AddFromInputStream(uiEvent.inputStream, fileName = uiEvent.fileName)
                }
            }
            is SubmitRenameDialogText -> AddNewDeckFeature.Wish.OfferName(uiEvent.dialogText)
            is CancelRenameDialog -> AddNewDeckFeature.Wish.Cancel
        }
    }

    val decksPreviewFeatureWish: (UiEvent) -> DecksPreviewFeature.Wish? = { uiEvent: UiEvent ->
        null
    }
}