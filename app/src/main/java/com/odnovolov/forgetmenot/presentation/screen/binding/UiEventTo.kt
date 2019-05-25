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
            is RenameDialogPositiveButtonClick -> AddNewDeckFeature.Wish.OfferName(uiEvent.dialogText)
            is RenameDialogNegativeButtonClick -> AddNewDeckFeature.Wish.Cancel
            else -> null
        }
    }

    val decksPreviewFeatureWish: (UiEvent) -> DecksPreviewFeature.Wish? = { uiEvent: UiEvent ->
        when (uiEvent) {
            is DeleteDeckButtonClick -> DecksPreviewFeature.Wish.DeleteDeck(uiEvent.idx)
            else -> null
        }
    }
}