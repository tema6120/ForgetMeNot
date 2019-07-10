package com.odnovolov.forgetmenot.presentation.screen.home

import android.os.Parcelable
import com.badoo.mvicore.android.AndroidTimeCapsule
import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.element.NewsPublisher
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.BaseFeature
import com.odnovolov.forgetmenot.domain.entity.Deck
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
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.News.NavigateToExercise
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.News.ShowDeckIsDeletedSnackbar
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.UiEvent.*
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.ViewState
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.parcel.Parcelize
import leakcanary.LeakSentry

class HomeScreenFeature(
    timeCapsule: AndroidTimeCapsule,
    decksExplorerFeature: DecksExplorerFeature,
    deleteDeckFeature: DeleteDeckFeature,
    exerciseCreatorFeature: ExerciseCreatorFeature
) : BaseFeature<UiEvent, Action, Effect, ViewState, News>(
    initialState = timeCapsule.get(HomeScreenFeature::class.java) ?: ViewState(),
    wishToAction = { HandleUiEvent(it) },
    bootstrapper = BootstrapperImpl(decksExplorerFeature, deleteDeckFeature, exerciseCreatorFeature),
    actor = ActorImpl(deleteDeckFeature, exerciseCreatorFeature),
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
        private val decksExplorerFeature: DecksExplorerFeature,
        private val deleteDeckFeature: DeleteDeckFeature,
        private val exerciseCreatorFeature: ExerciseCreatorFeature
    ) : Bootstrapper<Action> {
        override fun invoke(): Observable<Action> {
            return Observable.merge(
                listOf(
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
        data class AcceptDecksExplorerFeatureState(val state: DecksExplorerFeature.State) : Action()
        data class AcceptDeleteDeckFeatureNews(val news: DeleteDeckFeature.News) : Action()
        data class AcceptExerciseCreatorFeatureState(val state: ExerciseCreatorFeature.State) : Action()
        data class AcceptExerciseCreatorFeatureNews(val news: ExerciseCreatorFeature.News) : Action()
    }

    sealed class UiEvent {
        data class DeckButtonClicked(val idx: Int) : UiEvent()
        data class DeleteDeckButtonClicked(val idx: Int) : UiEvent()
        object DeckIsDeletedSnackbarCancelActionClicked : UiEvent()
    }

    class ActorImpl(
        deleteDeckFeature: DeleteDeckFeature,
        exerciseCreatorFeature: ExerciseCreatorFeature
    ) : Actor<ViewState, Action, Effect> {

        private val deleteDeckWishSender = PublishSubject.create<DeleteDeckFeature.Wish>()
            .apply { subscribe(deleteDeckFeature) }
        private val exerciseCreatorWishSender = PublishSubject.create<ExerciseCreatorFeature.Wish>()
            .apply { subscribe(exerciseCreatorFeature) }

        override fun invoke(viewState: ViewState, action: Action): Observable<Effect> {
            return when (action) {
                is HandleUiEvent -> handle(action.uiEvent)
                is AcceptDecksExplorerFeatureState -> accept(action.state)
                is AcceptDeleteDeckFeatureNews -> accept(action.news)
                is AcceptExerciseCreatorFeatureState -> accept(action.state)
                is AcceptExerciseCreatorFeatureNews -> accept(action.news)
            }
        }

        private fun handle(uiEvent: UiEvent): Observable<Effect> {
            when (uiEvent) {
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
        data class DecksPreviewUpdated(val decksPreview: List<DeckPreview>) : Effect()
        object DeckIsDeleted : Effect()
        object ExerciseCreatorIsInIdle : Effect()
        object ExerciseCreatorIsInProcessing : Effect()
        object ExerciseIsReady : Effect()
    }

    class ReducerImpl : Reducer<ViewState, Effect> {
        override fun invoke(viewState: ViewState, effect: Effect): ViewState = when (effect) {
            is DecksPreviewUpdated -> viewState.copy(
                decksPreview = effect.decksPreview
            )
            ExerciseCreatorIsInIdle -> viewState.copy(
                isProcessing = false
            )
            ExerciseCreatorIsInProcessing -> viewState.copy(
                isProcessing = true
            )
            ExerciseIsReady, DeckIsDeleted -> viewState
        }
    }

    @Parcelize
    data class ViewState(
        val decksPreview: List<DeckPreview> = emptyList(),
        val isProcessing: Boolean = false
    ) : Parcelable

    class NewsPublisherImpl : NewsPublisher<Action, Effect, ViewState, News> {
        override fun invoke(action: Action, effect: Effect, state: ViewState): News? {
            return when (effect) {
                ExerciseIsReady -> NavigateToExercise
                DeckIsDeleted -> ShowDeckIsDeletedSnackbar
                else -> null
            }
        }
    }

    sealed class News {
        object NavigateToExercise : News()
        object ShowDeckIsDeletedSnackbar : News()
    }

    override fun dispose() {
        super.dispose()
        LeakSentry.refWatcher.watch(this)
    }
}