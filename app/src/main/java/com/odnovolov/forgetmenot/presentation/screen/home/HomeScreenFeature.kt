package com.odnovolov.forgetmenot.presentation.screen.home

import android.os.Parcelable
import com.badoo.mvicore.android.AndroidTimeCapsule
import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.element.NewsPublisher
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.BaseFeature
import com.odnovolov.forgetmenot.domain.feature.adddeck.AddDeckFeature
import com.odnovolov.forgetmenot.domain.feature.adddeck.AddDeckFeature.State.Stage.*
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature.News.ExerciseIsPrepared
import com.odnovolov.forgetmenot.presentation.entity.DeckPreviewViewEntity
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.*
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.Action.*
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.Effect.*
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.News.NavigateToExercise
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.UiEvent.*
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenFeature.ViewState
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import leakcanary.LeakSentry
import java.io.InputStream
import kotlinx.android.parcel.Parcelize

class HomeScreenFeature(
    timeCapsule: AndroidTimeCapsule,
    addDeckFeature: AddDeckFeature,
    decksPreviewFeature: DecksPreviewFeature
) : BaseFeature<UiEvent, Action, Effect, ViewState, News>(
    initialState = timeCapsule.get(HomeScreenFeature::class.java) ?: ViewState(),
    wishToAction = { HandleUiEvent(it) },
    bootstrapper = BootstrapperImpl(addDeckFeature, decksPreviewFeature),
    actor = ActorImpl(addDeckFeature, decksPreviewFeature),
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
        private val decksPreviewFeature: DecksPreviewFeature
    ) : Bootstrapper<Action> {
        override fun invoke(): Observable<Action> {
            return Observable.merge(
                listOf(
                    Observable.wrap(addDeckFeature).map { AcceptAddDeckFeatureState(it) },
                    Observable.wrap(addDeckFeature.news).map { AcceptAddDeckFeatureNews(it) },
                    Observable.wrap(decksPreviewFeature).map { AcceptDecksPreviewFeatureState(it) },
                    Observable.wrap(decksPreviewFeature.news).map { AcceptDecksPreviewFeatureNews(it) }
                )
            )
        }
    }

    sealed class Action {
        data class HandleUiEvent(val uiEvent: UiEvent) : Action()
        data class AcceptAddDeckFeatureState(val state: AddDeckFeature.State) : Action()
        data class AcceptAddDeckFeatureNews(val news: AddDeckFeature.News) : Action()
        data class AcceptDecksPreviewFeatureState(val state: DecksPreviewFeature.State) : Action()
        data class AcceptDecksPreviewFeatureNews(val news: DecksPreviewFeature.News) : Action()
    }

    sealed class UiEvent {
        data class ContentReceived(val inputStream: InputStream, val fileName: String?) : UiEvent()
        data class RenameDialogPositiveButtonClicked(val dialogText: String) : UiEvent()
        object RenameDialogNegativeButtonClicked : UiEvent()
        data class DeckButtonClicked(val idx: Int) : UiEvent()
        data class DeleteDeckButtonClicked(val idx: Int) : UiEvent()
    }

    class ActorImpl(
        addDeckFeature: AddDeckFeature,
        decksPreviewFeature: DecksPreviewFeature
    ) : Actor<ViewState, Action, Effect> {

        private val addDeckWishSender = PublishSubject.create<AddDeckFeature.Wish>()
            .apply { subscribe(addDeckFeature) }
        private val decksPreviewWishSender = PublishSubject.create<DecksPreviewFeature.Wish>()
            .apply { subscribe(decksPreviewFeature) }

        override fun invoke(state: ViewState, action: Action): Observable<Effect> {
            return when (action) {
                is HandleUiEvent -> handle(action.uiEvent)
                is AcceptAddDeckFeatureState -> accept(action.state)
                is AcceptAddDeckFeatureNews -> Observable.empty()
                is AcceptDecksPreviewFeatureState -> accept(action.state)
                is AcceptDecksPreviewFeatureNews -> accept(action.news)
            }
        }

        private fun handle(uiEvent: UiEvent): Observable<Effect> {
            when (uiEvent) {
                is ContentReceived -> {
                    val wish: AddDeckFeature.Wish =
                        if (uiEvent.fileName == null) {
                            AddDeckFeature.Wish.AddFromInputStream(uiEvent.inputStream)
                        } else {
                            AddDeckFeature.Wish.AddFromInputStream(uiEvent.inputStream, fileName = uiEvent.fileName)
                        }
                    send(wish)
                }
                is RenameDialogPositiveButtonClicked -> {
                    send(AddDeckFeature.Wish.OfferName(uiEvent.dialogText))
                }
                RenameDialogNegativeButtonClicked -> {
                    send(AddDeckFeature.Wish.Cancel)
                }
                is DeckButtonClicked -> {
                    send(DecksPreviewFeature.Wish.PrepareExercise(uiEvent.idx))
                }
                is DeleteDeckButtonClicked -> {
                    send(DecksPreviewFeature.Wish.DeleteDeck(uiEvent.idx))
                }
            }
            return Observable.empty()
        }

        private fun send(wish: AddDeckFeature.Wish) {
            addDeckWishSender.onNext(wish)
        }

        private fun send(wish: DecksPreviewFeature.Wish) {
            decksPreviewWishSender.onNext(wish)
        }

        private fun accept(state: AddDeckFeature.State): Observable<Effect> {
            return when (state.stage) {
                Idle -> Observable.just(AddDeckIsInIdle as Effect)
                Processing -> Observable.just(AddDeckIsInProcessing as Effect)
                WaitingForName -> Observable.just(AddDeckIsInWaitingForName as Effect)
                is WaitingForChangingName -> Observable.just(AddDeckIsInWaitingForChangingName as Effect)
                Saving -> Observable.just(AddDeckIsInSaving as Effect)
            }
        }

        private fun accept(state: DecksPreviewFeature.State): Observable<Effect> {
            val decksPreview: List<DeckPreviewViewEntity> = state.decksPreview
                .map { deckPreview -> DeckPreviewViewEntity.fromDeckPreview(deckPreview) }
            return Observable.just(DecksPreviewWasReceived(decksPreview) as Effect)
        }

        private fun accept(news: DecksPreviewFeature.News): Observable<Effect> {
            return when (news) {
                ExerciseIsPrepared -> Observable.just(ExerciseIsPreparedNewsWasReceived as Effect)
            }
        }
    }

    sealed class Effect {
        object AddDeckIsInIdle : Effect()
        object AddDeckIsInProcessing : Effect()
        object AddDeckIsInWaitingForName : Effect()
        object AddDeckIsInWaitingForChangingName : Effect()
        object AddDeckIsInSaving : Effect()
        data class DecksPreviewWasReceived(val decksPreview: List<DeckPreviewViewEntity>) : Effect()
        object ExerciseIsPreparedNewsWasReceived : Effect()
    }

    class ReducerImpl : Reducer<ViewState, Effect> {
        override fun invoke(viewState: ViewState, effect: Effect): ViewState = when (effect) {
            AddDeckIsInIdle -> viewState.copy(
                isRenameDialogVisible = false,
                isProcessing = false
            )
            AddDeckIsInProcessing, AddDeckIsInSaving -> viewState.copy(
                isRenameDialogVisible = false,
                isProcessing = true
            )
            AddDeckIsInWaitingForName, AddDeckIsInWaitingForChangingName -> viewState.copy(
                isRenameDialogVisible = true,
                isProcessing = false
            )
            is DecksPreviewWasReceived -> viewState.copy(
                decksPreview = effect.decksPreview
            )
            ExerciseIsPreparedNewsWasReceived -> viewState
        }
    }

    @Parcelize
    data class ViewState(
        val decksPreview: List<DeckPreviewViewEntity> = emptyList(),
        val isRenameDialogVisible: Boolean = false,
        val isProcessing: Boolean = false
    ) : Parcelable

    class NewsPublisherImpl : NewsPublisher<Action, Effect, ViewState, News> {
        override fun invoke(action: Action, effect: Effect, state: ViewState): News? {
            return when (effect) {
                ExerciseIsPreparedNewsWasReceived -> NavigateToExercise
                else -> null
            }
        }
    }

    sealed class News {
        object NavigateToExercise : News()
    }

    override fun dispose() {
        super.dispose()
        LeakSentry.refWatcher.watch(this)
    }
}