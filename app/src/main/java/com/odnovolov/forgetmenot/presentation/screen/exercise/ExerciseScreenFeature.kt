package com.odnovolov.forgetmenot.presentation.screen.exercise

import android.os.Parcelable
import com.badoo.mvicore.android.AndroidTimeCapsule
import com.badoo.mvicore.element.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreenFeature.ViewState
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreenFeature.Action
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreenFeature.Action.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreenFeature.UiEvent
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreenFeature.Effect
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreenFeature.News
import com.badoo.mvicore.feature.BaseFeature
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature
import com.odnovolov.forgetmenot.presentation.entity.ExerciseCardViewEntity
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreenFeature.Effect.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreenFeature.News.MoveToNextPosition
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreenFeature.UiEvent.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.parcel.Parcelize
import leakcanary.LeakSentry

class ExerciseScreenFeature(
    timeCapsule: AndroidTimeCapsule,
    exerciseFeature: ExerciseFeature
) : BaseFeature<UiEvent, Action, Effect, ViewState, News>(
    initialState = timeCapsule.get(ExerciseScreenFeature::class.java) ?: ViewState(),
    wishToAction = { HandleUiEvent(it) },
    bootstrapper = BootstrapperImpl(exerciseFeature),
    actor = ActorImpl(exerciseFeature),
    reducer = ReducerImpl(),
    newsPublisher = NewsPublisherImpl()
) {
    init {
        timeCapsule.register(
            key = ExerciseScreenFeature::class.java,
            stateSupplier = { state }
        )
    }

    class BootstrapperImpl(
        private val exerciseFeature: ExerciseFeature
    ) : Bootstrapper<Action> {
        override fun invoke(): Observable<Action> {
            return Observable.wrap(exerciseFeature).map { AcceptExerciseFeatureState(it) }
        }
    }

    sealed class Action {
        data class HandleUiEvent(val uiEvent: UiEvent) : Action()
        data class AcceptExerciseFeatureState(val exerciseFeatureState: ExerciseFeature.State) : Action()
    }

    sealed class UiEvent {
        data class NewPageBecomesSelected(val position: Int) : UiEvent()
        object ShowAnswerButtonClick : UiEvent()
        object NotAskButtonClick : UiEvent()
        object UndoButtonClick : UiEvent()
    }

    class ActorImpl(
        private val exerciseFeature: ExerciseFeature
    ) : Actor<ViewState, Action, Effect> {

        private val wishSender = PublishSubject.create<ExerciseFeature.Wish>()
            .apply { subscribe(exerciseFeature) }

        override fun invoke(viewState: ViewState, action: Action): Observable<Effect> {
            return when (action) {
                is HandleUiEvent -> when (action.uiEvent) {
                    is NewPageBecomesSelected -> Observable.just(CurrentPositionChanged(action.uiEvent.position) as Effect)
                    is ShowAnswerButtonClick -> {
                        wishSender.onNext(ExerciseFeature.Wish.ShowAnswer(viewState.currentPosition!!))
                        Observable.empty()
                    }
                    is NotAskButtonClick -> {
                        wishSender.onNext(ExerciseFeature.Wish.SetCardAsLearned(viewState.currentPosition!!))
                        Observable.just(SetCardAsLearnedWishSent as Effect)
                    }
                    is UndoButtonClick -> {
                        wishSender.onNext(ExerciseFeature.Wish.SetCardAsUnlearned(viewState.currentPosition!!))
                        Observable.empty()
                    }
                }
                is AcceptExerciseFeatureState -> {
                    val newExerciseCards: List<ExerciseCardViewEntity> =
                        action.exerciseFeatureState.exerciseData.exerciseCards
                            .map { ExerciseCardViewEntity.fromExerciseCard(it) }
                    Observable.just(GotNewExerciseCards(newExerciseCards))
                }
            }
        }
    }

    sealed class Effect {
        data class CurrentPositionChanged(val position: Int) : Effect()
        object SetCardAsLearnedWishSent : Effect()
        data class GotNewExerciseCards(val exerciseCards: List<ExerciseCardViewEntity>) : Effect()
    }

    class ReducerImpl : Reducer<ViewState, Effect> {
        override fun invoke(viewState: ViewState, effect: Effect): ViewState = when (effect) {
            is GotNewExerciseCards -> viewState.copy(
                exerciseCards = effect.exerciseCards
            )
            is CurrentPositionChanged -> viewState.copy(
                currentPosition = effect.position
            )
            is SetCardAsLearnedWishSent -> viewState
        }
    }

    @Parcelize
    data class ViewState(
        val exerciseCards: List<ExerciseCardViewEntity> = emptyList(),
        val currentPosition: Int? = null
    ) : Parcelable

    class NewsPublisherImpl : NewsPublisher<Action, Effect, ViewState, News> {
        override fun invoke(action: Action, effect: Effect, viewState: ViewState): News? {
            return when {
                (effect is SetCardAsLearnedWishSent) && (!isLastPosition(viewState)) -> MoveToNextPosition
                else -> null
            }
        }

        private fun isLastPosition(viewState: ViewState): Boolean {
            val (exerciseCards, currentPosition) = viewState
            val lastPosition = exerciseCards.size - 1
            return currentPosition == lastPosition
        }
    }

    sealed class News {
        object MoveToNextPosition : News()
    }

    override fun dispose() {
        super.dispose()
        LeakSentry.refWatcher.watch(this)
    }
}