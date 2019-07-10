package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import android.os.Parcelable
import com.badoo.mvicore.android.AndroidTimeCapsule
import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.element.NewsPublisher
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.BaseFeature
import com.odnovolov.forgetmenot.domain.feature.adddeck.AddDeckFeature
import com.odnovolov.forgetmenot.domain.feature.adddeck.AddDeckFeature.News.*
import com.odnovolov.forgetmenot.domain.feature.adddeck.AddDeckFeature.State.Stage.*
import com.odnovolov.forgetmenot.domain.feature.adddeck.AddDeckFeature.Wish.*
import com.odnovolov.forgetmenot.domain.feature.decksexplorer.DecksExplorerFeature
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenFeature.*
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenFeature.Action.*
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenFeature.Effect.*
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenFeature.News.*
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenFeature.UiEvent.*
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenFeature.ViewState
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.parcel.Parcelize
import leakcanary.LeakSentry
import java.io.InputStream

class AddDeckScreenFeature(
    timeCapsule: AndroidTimeCapsule,
    addDeckFeature: AddDeckFeature,
    decksExplorerFeature: DecksExplorerFeature
) : BaseFeature<UiEvent, Action, Effect, ViewState, News>(
    initialState = timeCapsule.get(AddDeckScreenFeature::class.java) ?: ViewState(),
    wishToAction = { HandleUiEvent(it) },
    bootstrapper = BootstrapperImpl(addDeckFeature, decksExplorerFeature),
    actor = ActorImpl(addDeckFeature),
    reducer = ReducerImpl(),
    newsPublisher = NewsPublisherImpl()
) {
    init {
        timeCapsule.register(
            key = AddDeckScreenFeature::class.java,
            stateSupplier = { state }
        )
    }

    class BootstrapperImpl(
        private val addDeckFeature: AddDeckFeature,
        private val decksExplorerFeature: DecksExplorerFeature
    ) : Bootstrapper<Action> {
        override fun invoke(): Observable<Action> {
            return Observable.merge(
                listOf(
                    Observable.wrap(addDeckFeature).map { AcceptAddDeckFeatureState(it) },
                    Observable.wrap(addDeckFeature.news).map { AcceptAddDeckFeatureNews(it) },
                    Observable.wrap(decksExplorerFeature).map { AcceptDecksExplorerFeatureState(it) }
                )
            )
        }
    }

    sealed class Action {
        data class HandleUiEvent(val uiEvent: UiEvent) : Action()
        data class AcceptAddDeckFeatureState(val state: AddDeckFeature.State) : Action()
        data class AcceptAddDeckFeatureNews(val news: AddDeckFeature.News) : Action()
        data class AcceptDecksExplorerFeatureState(val state: DecksExplorerFeature.State) : Action()
    }

    sealed class UiEvent {
        object AddButtonClicked : UiEvent()
        data class ContentReceived(val inputStream: InputStream, val fileName: String?) : UiEvent()
        data class DialogTextChanged(val text: String) : UiEvent()
        object PositiveDialogButtonClicked : UiEvent()
        object NegativeDialogButtonClicked : UiEvent()
    }

    class ActorImpl(
        addDeckFeature: AddDeckFeature
    ) : Actor<ViewState, Action, Effect> {

        private val wishSender = PublishSubject.create<AddDeckFeature.Wish>()
            .apply { subscribe(addDeckFeature) }

        override fun invoke(viewState: ViewState, action: Action): Observable<Effect> {
            return when (action) {
                is HandleUiEvent -> handle(action.uiEvent, viewState)
                is AcceptAddDeckFeatureState -> accept(action.state)
                is AcceptAddDeckFeatureNews -> accept(action.news)
                is AcceptDecksExplorerFeatureState -> accept(action.state)
            }
        }

        private fun handle(uiEvent: UiEvent, viewState: ViewState): Observable<Effect> {
            return when (uiEvent) {
                AddButtonClicked -> Observable.just(AddButtonWasClicked)
                is ContentReceived -> {
                    val wish =
                        if (uiEvent.fileName == null) {
                            AddFromInputStream(uiEvent.inputStream)
                        } else {
                            AddFromInputStream(uiEvent.inputStream, fileName = uiEvent.fileName)
                        }
                    wishSender.onNext(wish)
                    Observable.empty()
                }
                is DialogTextChanged -> {
                    val enteredText = uiEvent.text
                    val effect = when {
                        enteredText.isEmpty() -> EnteredDeckNameIsEmpty
                        viewState.occupiedDeckNames.any { it == enteredText } -> EnteredDeckNameIsOccupied(enteredText)
                        else -> EnteredDeckNameIsOkay(enteredText)
                    }
                    Observable.just(effect)
                }
                PositiveDialogButtonClicked -> {
                    wishSender.onNext(OfferName(viewState.enteredDeckName))
                    Observable.empty()
                }
                NegativeDialogButtonClicked -> {
                    wishSender.onNext(Cancel)
                    Observable.empty()
                }
            }
        }

        private fun accept(state: AddDeckFeature.State): Observable<Effect> {
            return when (state.stage) {
                Idle -> Observable.just(AddDeckIsInIdle)
                Parsing -> Observable.just(AddDeckIsInParsing)
                WaitingForName -> Observable.just(AddDeckIsInWaitingForName)
                is WaitingForChangingName -> Observable.just(AddDeckIsInWaitingForName)
                Saving -> Observable.just(AddDeckIsInSaving)
            }
        }

        private fun accept(news: AddDeckFeature.News): Observable<Effect> {
            return when (news) {
                is ErrorHappened -> Observable.just(ErrorWasHappened(news.message))
                is IncorrectDeckName -> Observable.just(DeckNameIsRequested(news.cause))
                else -> Observable.empty()
            }
        }

        private fun accept(state: DecksExplorerFeature.State): Observable<Effect> {
            val occupiedDeckNames = state.decks.map { it.name }
            return Observable.just(OccupiedDeckNamesUpdated(occupiedDeckNames))
        }
    }

    sealed class Effect {
        data class OccupiedDeckNamesUpdated(val occupiedDeckNames: List<String>) : Effect()
        object AddButtonWasClicked : Effect()
        object AddDeckIsInIdle : Effect()
        object AddDeckIsInParsing : Effect()
        object AddDeckIsInWaitingForName : Effect()
        object AddDeckIsInSaving : Effect()
        data class ErrorWasHappened(val errorMessage: String) : Effect()
        data class DeckNameIsRequested(val cause: IncorrectDeckName.Cause) : Effect()
        object EnteredDeckNameIsEmpty : Effect()
        data class EnteredDeckNameIsOccupied(val enteredName: String) : Effect()
        data class EnteredDeckNameIsOkay(val enteredName: String) : Effect()
    }

    class ReducerImpl : Reducer<ViewState, Effect> {
        override fun invoke(viewState: ViewState, effect: Effect): ViewState = when (effect) {
            is OccupiedDeckNamesUpdated -> viewState.copy(
                occupiedDeckNames = effect.occupiedDeckNames
            )
            AddButtonWasClicked -> viewState
            AddDeckIsInIdle -> viewState.copy(
                isDialogVisible = false,
                isProcessing = false
            )
            AddDeckIsInParsing -> viewState.copy(
                isDialogVisible = false,
                isProcessing = true
            )
            AddDeckIsInWaitingForName -> viewState.copy(
                isDialogVisible = true,
                isProcessing = false
            )
            AddDeckIsInSaving -> viewState.copy(
                isDialogVisible = false,
                isProcessing = true
            )
            is ErrorWasHappened, is DeckNameIsRequested -> viewState
            EnteredDeckNameIsEmpty -> viewState.copy(
                enteredDeckName = "",
                errorText = "Name cannot be empty",
                isPositiveButtonEnabled = false
            )
            is EnteredDeckNameIsOccupied -> viewState.copy(
                enteredDeckName = effect.enteredName,
                errorText = "This name is occupied",
                isPositiveButtonEnabled = false
            )
            is EnteredDeckNameIsOkay -> viewState.copy(
                enteredDeckName = effect.enteredName,
                errorText = null,
                isPositiveButtonEnabled = true
            )
        }
    }

    @Parcelize
    data class ViewState(
        val occupiedDeckNames: List<String> = emptyList(),
        val isProcessing: Boolean = false,
        val isDialogVisible: Boolean = false,
        val enteredDeckName: String = "",
        val errorText: String? = null,
        val isPositiveButtonEnabled: Boolean = false
    ) : Parcelable

    class NewsPublisherImpl : NewsPublisher<Action, Effect, ViewState, News> {
        override fun invoke(action: Action, effect: Effect, state: ViewState): News? {
            return when (effect) {
                AddButtonWasClicked -> ShowFileChooser
                is ErrorWasHappened -> ShowToast(effect.errorMessage)
                is DeckNameIsRequested -> {
                    when (effect.cause) {
                        IncorrectDeckName.Cause.NameIsEmpty -> SetDialogText("")
                        is IncorrectDeckName.Cause.NameIsOccupied -> SetDialogText(effect.cause.occupiedName)
                    }
                }
                else -> null
            }
        }
    }

    sealed class News {
        object ShowFileChooser : News()
        data class ShowToast(val text: String) : News()
        data class SetDialogText(val text: String) : News()
    }

    override fun dispose() {
        super.dispose()
        LeakSentry.refWatcher.watch(this)
    }
}