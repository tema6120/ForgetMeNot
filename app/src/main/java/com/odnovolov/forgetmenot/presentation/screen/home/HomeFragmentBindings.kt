package com.odnovolov.forgetmenot.presentation.screen.home

import com.badoo.mvicore.binder.Binder
import com.badoo.mvicore.binder.using
import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature
import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature.State.Stage.*
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DeckPreview
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature.News.ExerciseIsPrepared
import com.odnovolov.forgetmenot.presentation.common.Combo
import com.odnovolov.forgetmenot.presentation.common.Combo.DoubleState
import com.odnovolov.forgetmenot.presentation.common.LifecycleScope.CREATE_DESTROY
import com.odnovolov.forgetmenot.presentation.common.LifecycleScope.START_STOP
import com.odnovolov.forgetmenot.presentation.common.adaptForBinder
import com.odnovolov.forgetmenot.presentation.screen.home.HomeFragment.UiEvent
import com.odnovolov.forgetmenot.presentation.screen.home.HomeFragment.UiEvent.*
import com.odnovolov.forgetmenot.presentation.screen.home.HomeFragment.ViewState

class HomeFragmentBindings(
    private val addNewDeckFeature: AddNewDeckFeature,
    private val decksPreviewFeature: DecksPreviewFeature
) {
    fun setup(fragment: HomeFragment) {
        Binder(fragment.lifecycle.adaptForBinder(CREATE_DESTROY)).run {
            bind(fragment to addNewDeckFeature using UiEventToAddDeckWish)
            bind(fragment to decksPreviewFeature using UiEventToDecksPreviewWish)
        }
        Binder(fragment.lifecycle.adaptForBinder(START_STOP)).run {
            bind(Combo.of(addNewDeckFeature, decksPreviewFeature) to fragment using ViewStateAdapter)
            bind(decksPreviewFeature.news to fragment.newsConsumer using NewsTransformer)
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

    private object UiEventToAddDeckWish : (UiEvent) -> AddNewDeckFeature.Wish? {
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

    private object UiEventToDecksPreviewWish : (UiEvent) -> DecksPreviewFeature.Wish? {
        override fun invoke(uiEvent: UiEvent): DecksPreviewFeature.Wish? {
            return when (uiEvent) {
                is DeckButtonClick -> DecksPreviewFeature.Wish.PrepareExercise(uiEvent.idx)
                is DeleteDeckButtonClick -> DecksPreviewFeature.Wish.DeleteDeck(uiEvent.idx)
                else -> null
            }
        }
    }

    private object NewsTransformer : (DecksPreviewFeature.News) -> HomeFragment.News? {
        override fun invoke(featureNews: DecksPreviewFeature.News): HomeFragment.News? {
            return when (featureNews) {
                is ExerciseIsPrepared -> HomeFragment.News.NavigateToExercise
            }
        }
    }
}