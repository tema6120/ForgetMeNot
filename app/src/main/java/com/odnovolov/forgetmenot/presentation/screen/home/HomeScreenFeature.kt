package com.odnovolov.forgetmenot.presentation.screen.home

import android.os.Parcelable
import com.badoo.mvicore.android.AndroidTimeCapsule
import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.element.NewsPublisher
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.BaseFeature
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.feature.adddeck.AddDeckFeature
import com.odnovolov.forgetmenot.domain.feature.adddeck.AddDeckFeature.News.IncorrectDeckName
import com.odnovolov.forgetmenot.domain.feature.adddeck.AddDeckFeature.State.Stage.*
import com.odnovolov.forgetmenot.domain.feature.adddeck.AddDeckFeature.Wish.*
import com.odnovolov.forgetmenot.domain.feature.decksexplorer.DecksExplorerFeature
import com.odnovolov.forgetmenot.domain.feature.deletedeck.DeleteDeckFeature
import com.odnovolov.forgetmenot.domain.feature.deletedeck.DeleteDeckFeature.News.*
import com.odnovolov.forgetmenot.domain.feature.deletedeck.DeleteDeckFeature.Wish.DeleteDeck
import com.odnovolov.forgetmenot.domain.feature.deletedeck.DeleteDeckFeature.Wish.RestoreDeck
import com.odnovolov.forgetmenot.domain.feature.exercisecreator.ExerciseCreatorFeature
import com.odnovolov.forgetmenot.domain.feature.exercisecreator.ExerciseCreatorFeature.News.ExerciseCreated
import com.odnovolov.forgetmenot.domain.feature.exercisecreator.ExerciseCreatorFeature.Wish.CreateExercise
import com.odnovolov.forgetmenot.presentation.entity.DeckPreview
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.*
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.Action.*
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.Effect.*
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.News.*
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.UiEvent.*
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.ViewState
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.ViewState.DeckNameInputDialogState
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.parcel.Parcelize
import leakcanary.LeakSentry
import java.io.InputStream

class HomeScreenFeature(
    timeCapsule: AndroidTimeCapsule,
    addDeckFeature: AddDeckFeature,
    decksExplorerFeature: DecksExplorerFeature,
    deleteDeckFeature: DeleteDeckFeature,
    exerciseCreatorFeature: ExerciseCreatorFeature
) : BaseFeature<UiEvent, Action, Effect, ViewState, News>(
    initialState = timeCapsule.get(HomeScreenFeature::class.java) ?: ViewState(),
    wishToAction = { HandleUiEvent(it) },
    bootstrapper = BootstrapperImpl(addDeckFeature, decksExplorerFeature, deleteDeckFeature, exerciseCreatorFeature),
    actor = ActorImpl(addDeckFeature, deleteDeckFeature, exerciseCreatorFeature),
    reducer = ReducerImpl(),
    newsPublisher = NewsPublisherImpl()
) {
    init {
        timeCapsule.register(
            key = HomeScreenFeature::class.java,
            stateSupplier = { state }
        )
    }

    class BootstrapperImpl(
        private val addDeckFeature: AddDeckFeature,
        private val decksExplorerFeature: DecksExplorerFeature,
        private val deleteDeckFeature: DeleteDeckFeature,
        private val exerciseCreatorFeature: ExerciseCreatorFeature
    ) : Bootstrapper<Action> {
        override fun invoke(): Observable<Action> {
            return Observable.merge(
                listOf(
                    Observable.wrap(addDeckFeature).map { AcceptAddDeckFeatureState(it) },
                    Observable.wrap(addDeckFeature.news).map { AcceptAddDeckFeatureNews(it) },
                    Observable.wrap(decksExplorerFeature).map { AcceptDecksExplorerFeatureState(it) },
                    Observable.wrap(deleteDeckFeature.news).map { AcceptDeleteDeckFeatureNews(it) },
                    Observable.wrap(exerciseCreatorFeature).map { AcceptExerciseCreatorFeatureState(it) },
                    Observable.wrap(exerciseCreatorFeature.news).map { AcceptExerciseCreatorFeatureNews(it) }
                )
            )
        }
    }

    sealed class Action {
        data class HandleUiEvent(val uiEvent: UiEvent) : Action()
        data class AcceptAddDeckFeatureState(val state: AddDeckFeature.State) : Action()
        data class AcceptAddDeckFeatureNews(val news: AddDeckFeature.News) : Action()
        data class AcceptDecksExplorerFeatureState(val state: DecksExplorerFeature.State) : Action()
        data class AcceptDeleteDeckFeatureNews(val news: DeleteDeckFeature.News) : Action()
        data class AcceptExerciseCreatorFeatureState(val state: ExerciseCreatorFeature.State) : Action()
        data class AcceptExerciseCreatorFeatureNews(val news: ExerciseCreatorFeature.News) : Action()
    }

    sealed class UiEvent {
        object AddButtonClicked : UiEvent()
        data class ContentReceived(val inputStream: InputStream, val fileName: String?) : UiEvent()
        data class DeckNameInputTextChanged(val text: String) : UiEvent()
        object PositiveDeckNameInputDialogButtonClicked : UiEvent()
        object NegativeDeckNameInputDialogButtonClicked : UiEvent()
        data class DeckButtonClicked(val idx: Int) : UiEvent()
        data class DeleteDeckButtonClicked(val idx: Int) : UiEvent()
        object DeckIsDeletedSnackbarCancelActionClicked : UiEvent()
    }

    class ActorImpl(
        addDeckFeature: AddDeckFeature,
        deleteDeckFeature: DeleteDeckFeature,
        exerciseCreatorFeature: ExerciseCreatorFeature
    ) : Actor<ViewState, Action, Effect> {

        private val addDeckWishSender = PublishSubject.create<AddDeckFeature.Wish>()
            .apply { subscribe(addDeckFeature) }
        private val deleteDeckWishSender = PublishSubject.create<DeleteDeckFeature.Wish>()
            .apply { subscribe(deleteDeckFeature) }
        private val exerciseCreatorWishSender = PublishSubject.create<ExerciseCreatorFeature.Wish>()
            .apply { subscribe(exerciseCreatorFeature) }

        override fun invoke(viewState: ViewState, action: Action): Observable<Effect> {
            return when (action) {
                is HandleUiEvent -> handle(action.uiEvent, viewState)
                is AcceptAddDeckFeatureState -> accept(action.state)
                is AcceptAddDeckFeatureNews -> accept(action.news)
                is AcceptDecksExplorerFeatureState -> accept(action.state)
                is AcceptDeleteDeckFeatureNews -> accept(action.news)
                is AcceptExerciseCreatorFeatureState -> accept(action.state)
                is AcceptExerciseCreatorFeatureNews -> accept(action.news)
            }
        }

        private fun handle(uiEvent: UiEvent, viewState: ViewState): Observable<Effect> {
            when (uiEvent) {
                AddButtonClicked -> return Observable.just(AddButtonWasClicked)
                is ContentReceived -> {
                    val wish: AddDeckFeature.Wish =
                        if (uiEvent.fileName == null) {
                            AddFromInputStream(uiEvent.inputStream)
                        } else {
                            AddFromInputStream(uiEvent.inputStream, fileName = uiEvent.fileName)
                        }
                    addDeckWishSender.onNext(wish)
                }
                is DeckNameInputTextChanged -> {
                    val userText = uiEvent.text
                    val deckNameInputDialogState = when {
                        userText.isEmpty() -> {
                            DeckNameInputDialogState(
                                isVisible = true,
                                inputText = userText,
                                errorText = "Name cannot be empty",
                                isPositiveButtonEnabled = false
                            )
                        }
                        viewState.decksPreview.any { it.deckName == userText } -> {
                            DeckNameInputDialogState(
                                isVisible = true,
                                inputText = userText,
                                errorText = "This name is occupied",
                                isPositiveButtonEnabled = false
                            )
                        }
                        else -> {
                            DeckNameInputDialogState(
                                isVisible = true,
                                inputText = userText,
                                errorText = null,
                                isPositiveButtonEnabled = true
                            )
                        }
                    }
                    return Observable.just(DeckNameInputDialogStateChanged(deckNameInputDialogState))
                }
                PositiveDeckNameInputDialogButtonClicked -> {
                    addDeckWishSender.onNext(OfferName(viewState.deckNameInputDialogState.inputText))
                }
                NegativeDeckNameInputDialogButtonClicked -> {
                    addDeckWishSender.onNext(Cancel)
                }
                is DeckButtonClicked -> {
                    exerciseCreatorWishSender.onNext(CreateExercise(uiEvent.idx))
                }
                is DeleteDeckButtonClicked -> {
                    deleteDeckWishSender.onNext(DeleteDeck(uiEvent.idx))
                }
                DeckIsDeletedSnackbarCancelActionClicked -> {
                    deleteDeckWishSender.onNext(RestoreDeck)
                }
            }
            return Observable.empty()
        }

        private fun accept(state: AddDeckFeature.State): Observable<Effect> {
            return when (state.stage) {
                Idle -> Observable.just(AddDeckIsInIdle as Effect)
                Parsing -> Observable.just(AddDeckIsInParsing as Effect)
                WaitingForName -> Observable.just(AddDeckIsInWaitingForName as Effect)
                is WaitingForChangingName -> Observable.just(AddDeckIsInWaitingForChangingName as Effect)
                Saving -> Observable.just(AddDeckIsInSaving as Effect)
            }
        }

        private fun accept(news: AddDeckFeature.News): Observable<Effect> {
            return when (news) {
                is IncorrectDeckName -> Observable.just(IncorrectDeckNameNewsReceived(news.cause))
                else -> Observable.empty()
            }
        }

        private fun accept(state: DecksExplorerFeature.State): Observable<Effect> {
            val decksPreview: List<DeckPreview> = state.decks
                .map { deck: Deck ->
                    val passedLaps: Int = deck.cards
                        .filter { card -> !card.isLearned }
                        .map { card -> card.lap }
                        .min() ?: 0
                    val progress = DeckPreview.Progress(
                        learned = deck.cards.filter { it.isLearned }.size,
                        total = deck.cards.size
                    )
                    DeckPreview(
                        deck.id,
                        deck.name,
                        passedLaps,
                        progress
                    )
                }
            return Observable.just(DecksPreviewUpdated(decksPreview))
        }

        private fun accept(news: DeleteDeckFeature.News): Observable<Effect> {
            return when (news) {
                DeckDeleted -> Observable.just(DeckIsDeleted)
                DeckIsNotDeleted -> Observable.empty()
                DeckRestored -> Observable.empty()
            }
        }

        private fun accept(state: ExerciseCreatorFeature.State): Observable<Effect> {
            return Observable.just(
                if (state.isProcessing) ExerciseCreatorIsInProcessing
                else ExerciseCreatorIsInIdle
            )
        }

        private fun accept(news: ExerciseCreatorFeature.News): Observable<Effect> {
            return when (news) {
                ExerciseCreated -> Observable.just(ExerciseIsReady as Effect)
            }
        }
    }

    sealed class Effect {
        object AddButtonWasClicked : Effect()
        object AddDeckIsInIdle : Effect()
        object AddDeckIsInParsing : Effect()
        object AddDeckIsInWaitingForName : Effect()
        object AddDeckIsInWaitingForChangingName : Effect()
        object AddDeckIsInSaving : Effect()
        data class IncorrectDeckNameNewsReceived(val cause: IncorrectDeckName.Cause) : Effect()
        data class DeckNameInputDialogStateChanged(val deckNameInputDialogState: DeckNameInputDialogState) : Effect()
        data class DecksPreviewUpdated(val decksPreview: List<DeckPreview>) : Effect()
        object DeckIsDeleted : Effect()
        object ExerciseCreatorIsInIdle : Effect()
        object ExerciseCreatorIsInProcessing : Effect()
        object ExerciseIsReady : Effect()
    }

    class ReducerImpl : Reducer<ViewState, Effect> {
        override fun invoke(viewState: ViewState, effect: Effect): ViewState = when (effect) {
            AddDeckIsInIdle -> viewState.copy(
                deckNameInputDialogState = viewState.deckNameInputDialogState.copy(isVisible = false),
                isProcessing = false
            )
            AddDeckIsInParsing, AddDeckIsInSaving -> viewState.copy(
                deckNameInputDialogState = viewState.deckNameInputDialogState.copy(isVisible = false),
                isProcessing = true
            )
            AddDeckIsInWaitingForName, AddDeckIsInWaitingForChangingName -> viewState.copy(
                deckNameInputDialogState = viewState.deckNameInputDialogState.copy(isVisible = true),
                isProcessing = false
            )
            is DeckNameInputDialogStateChanged -> viewState.copy(
                deckNameInputDialogState = effect.deckNameInputDialogState
            )
            is DecksPreviewUpdated -> viewState.copy(
                decksPreview = effect.decksPreview
            )
            ExerciseCreatorIsInIdle -> viewState.copy(
                isProcessing = false
            )
            ExerciseCreatorIsInProcessing -> viewState.copy(
                isProcessing = true
            )
            AddButtonWasClicked, ExerciseIsReady, DeckIsDeleted, is IncorrectDeckNameNewsReceived -> viewState
        }
    }

    @Parcelize
    data class ViewState(
        val decksPreview: List<DeckPreview> = emptyList(),
        val deckNameInputDialogState: DeckNameInputDialogState = DeckNameInputDialogState(),
        val isProcessing: Boolean = false
    ) : Parcelable {

        @Parcelize
        data class DeckNameInputDialogState(
            val isVisible: Boolean = false,
            val inputText: String = "",
            val errorText: String? = null,
            val isPositiveButtonEnabled: Boolean = false
        ) : Parcelable
    }

    class NewsPublisherImpl : NewsPublisher<Action, Effect, ViewState, News> {
        override fun invoke(action: Action, effect: Effect, state: ViewState): News? {
            return when (effect) {
                AddButtonWasClicked -> ShowFileChooser
                ExerciseIsReady -> NavigateToExercise
                DeckIsDeleted -> ShowDeckIsDeletedSnackbar
                is IncorrectDeckNameNewsReceived -> {
                    when (effect.cause) {
                        IncorrectDeckName.Cause.NameIsEmpty ->
                            SetDeckNameInputDialogText("")
                        is IncorrectDeckName.Cause.NameIsOccupied ->
                            SetDeckNameInputDialogText(effect.cause.occupiedName)
                    }
                }
                else -> null
            }
        }
    }

    sealed class News {
        object ShowFileChooser : News()
        object NavigateToExercise : News()
        object ShowDeckIsDeletedSnackbar : News()
        data class SetDeckNameInputDialogText(val text: String) : News()
    }

    override fun dispose() {
        super.dispose()
        LeakSentry.refWatcher.watch(this)
    }
}