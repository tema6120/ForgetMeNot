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
import com.odnovolov.forgetmenot.domain.feature.adddeck.AddDeckFeature.Wish.*
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature
import com.odnovolov.forgetmenot.domain.feature.deletedeck.DeleteDeckFeature
import com.odnovolov.forgetmenot.domain.feature.deletedeck.DeleteDeckFeature.Wish.DeleteDeck
import com.odnovolov.forgetmenot.domain.feature.exercisecreator.ExerciseCreatorFeature
import com.odnovolov.forgetmenot.domain.feature.exercisecreator.ExerciseCreatorFeature.News.ExerciseCreated
import com.odnovolov.forgetmenot.domain.feature.exercisecreator.ExerciseCreatorFeature.Wish.CreateExercise
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
    decksPreviewFeature: DecksPreviewFeature,
    deleteDeckFeature: DeleteDeckFeature,
    exerciseCreatorFeature: ExerciseCreatorFeature
) : BaseFeature<UiEvent, Action, Effect, ViewState, News>(
    initialState = timeCapsule.get(HomeScreenFeature::class.java) ?: ViewState(),
    wishToAction = { HandleUiEvent(it) },
    bootstrapper = BootstrapperImpl(addDeckFeature, decksPreviewFeature, exerciseCreatorFeature),
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
        private val decksPreviewFeature: DecksPreviewFeature,
        private val exerciseCreatorFeature: ExerciseCreatorFeature
    ) : Bootstrapper<Action> {
        override fun invoke(): Observable<Action> {
            return Observable.merge(
                listOf(
                    Observable.wrap(addDeckFeature).map { AcceptAddDeckFeatureState(it) },
                    Observable.wrap(addDeckFeature.news).map { AcceptAddDeckFeatureNews(it) },
                    Observable.wrap(decksPreviewFeature).map { AcceptDecksPreviewFeatureState(it) },
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
        data class AcceptDecksPreviewFeatureState(val state: DecksPreviewFeature.State) : Action()
        data class AcceptExerciseCreatorFeatureState(val state: ExerciseCreatorFeature.State) : Action()
        data class AcceptExerciseCreatorFeatureNews(val news: ExerciseCreatorFeature.News) : Action()
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
        deleteDeckFeature: DeleteDeckFeature,
        exerciseCreatorFeature: ExerciseCreatorFeature
    ) : Actor<ViewState, Action, Effect> {

        private val addDeckWishSender = PublishSubject.create<AddDeckFeature.Wish>()
            .apply { subscribe(addDeckFeature) }
        private val deleteDeckWishSender = PublishSubject.create<DeleteDeckFeature.Wish>()
            .apply { subscribe(deleteDeckFeature) }
        private val exerciseCreatorWishSender = PublishSubject.create<ExerciseCreatorFeature.Wish>()
            .apply { subscribe(exerciseCreatorFeature) }

        override fun invoke(state: ViewState, action: Action): Observable<Effect> {
            return when (action) {
                is HandleUiEvent -> handle(action.uiEvent)
                is AcceptAddDeckFeatureState -> accept(action.state)
                is AcceptAddDeckFeatureNews -> Observable.empty()
                is AcceptDecksPreviewFeatureState -> accept(action.state)
                is AcceptExerciseCreatorFeatureState -> accept(action.state)
                is AcceptExerciseCreatorFeatureNews -> accept(action.news)
            }
        }

        private fun handle(uiEvent: UiEvent): Observable<Effect> {
            when (uiEvent) {
                is ContentReceived -> {
                    val wish: AddDeckFeature.Wish =
                        if (uiEvent.fileName == null) {
                            AddFromInputStream(uiEvent.inputStream)
                        } else {
                            AddFromInputStream(uiEvent.inputStream, fileName = uiEvent.fileName)
                        }
                    addDeckWishSender.onNext(wish)
                }
                is RenameDialogPositiveButtonClicked -> {
                    addDeckWishSender.onNext(OfferName(uiEvent.dialogText))
                }
                RenameDialogNegativeButtonClicked -> {
                    addDeckWishSender.onNext(Cancel)
                }
                is DeckButtonClicked -> {
                    exerciseCreatorWishSender.onNext(CreateExercise(uiEvent.idx))
                }
                is DeleteDeckButtonClicked -> {
                    deleteDeckWishSender.onNext(DeleteDeck(uiEvent.idx))
                }
            }
            return Observable.empty()
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

        private fun accept(state: ExerciseCreatorFeature.State): Observable<Effect> {
            return Observable.just(
                if (state.isProcessing) ExerciseIsInProcessing
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
        object AddDeckIsInIdle : Effect()
        object AddDeckIsInProcessing : Effect()
        object AddDeckIsInWaitingForName : Effect()
        object AddDeckIsInWaitingForChangingName : Effect()
        object AddDeckIsInSaving : Effect()
        data class DecksPreviewWasReceived(val decksPreview: List<DeckPreviewViewEntity>) : Effect()
        object ExerciseCreatorIsInIdle : Effect()
        object ExerciseIsInProcessing : Effect()
        object ExerciseIsReady : Effect()
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
            ExerciseCreatorIsInIdle -> viewState.copy(
                isProcessing = false
            )
            ExerciseIsInProcessing -> viewState.copy(
                isProcessing = true
            )
            ExerciseIsReady -> viewState
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
                ExerciseIsReady -> NavigateToExercise
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