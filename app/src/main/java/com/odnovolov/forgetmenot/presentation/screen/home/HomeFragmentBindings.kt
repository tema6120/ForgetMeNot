package com.odnovolov.forgetmenot.presentation.screen.home

import com.badoo.mvicore.binder.Binder
import com.badoo.mvicore.binder.using
import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature
import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature.State.Stage.*
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DeckPreview
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature
import com.odnovolov.forgetmenot.presentation.common.Combo
import com.odnovolov.forgetmenot.presentation.common.Combo.DoubleState
import com.odnovolov.forgetmenot.presentation.common.adaptForBinder
import com.odnovolov.forgetmenot.presentation.navigation.NavigationEventFinder
import com.odnovolov.forgetmenot.presentation.navigation.Navigator
import com.odnovolov.forgetmenot.presentation.screen.home.HomeFragment.UiEvent
import com.odnovolov.forgetmenot.presentation.screen.home.HomeFragment.UiEvent.*
import com.odnovolov.forgetmenot.presentation.screen.home.HomeFragment.ViewState

class HomeFragmentBindings(
    private val addNewDeckFeature: AddNewDeckFeature,
    private val decksPreviewFeature: DecksPreviewFeature,
    private val navigator: Navigator
) {
    fun setup(fragment: HomeFragment) {
        val lifecycle = fragment.lifecycle.adaptForBinder()
        Binder(lifecycle).run {
            bind(Combo.of(addNewDeckFeature, decksPreviewFeature) to fragment using ViewStateAdapter)
            bind(fragment to addNewDeckFeature using AddDeckWishReadingAbility)
            bind(fragment to decksPreviewFeature using DecksPreviewWishReadingAbility)
            bind(decksPreviewFeature.news to navigator using NavigationEventFinder.fromDecksPreviewFeature)
        }
    }

    private object ViewStateAdapter : (DoubleState<AddNewDeckFeature.State, DecksPreviewFeature.State>) -> ViewState {
        override fun invoke(doubleState: DoubleState<AddNewDeckFeature.State, DecksPreviewFeature.State>): ViewState {
            val (addDeckState, decksPreviewState) = doubleState

            val decksPreview: List<DeckPreview>
            val isRenameDialogVisible: Boolean
            val isProcessing: Boolean

            decksPreview = decksPreviewState.decksPreview
            when (addDeckState.stage) {
                is Idle -> {
                    isRenameDialogVisible = false
                    isProcessing = false
                }
                is Processing, is Saving -> {
                    isRenameDialogVisible = false
                    isProcessing = true
                }
                is WaitingForName, is WaitingForChangingName -> {
                    isRenameDialogVisible = true
                    isProcessing = false
                }
            }

            return ViewState(
                decksPreview,
                isRenameDialogVisible,
                isProcessing
            )
        }
    }

    private object AddDeckWishReadingAbility : (UiEvent) -> AddNewDeckFeature.Wish? {
        override fun invoke(uiEvent: UiEvent): AddNewDeckFeature.Wish? {
            return when (uiEvent) {
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
    }

    private object DecksPreviewWishReadingAbility : (UiEvent) -> DecksPreviewFeature.Wish? {
        override fun invoke(uiEvent: UiEvent): DecksPreviewFeature.Wish? {
            return when (uiEvent) {
                is DeckButtonClick -> DecksPreviewFeature.Wish.PrepareExercise(uiEvent.idx)
                is DeleteDeckButtonClick -> DecksPreviewFeature.Wish.DeleteDeck(uiEvent.idx)
                else -> null
            }
        }
    }
}