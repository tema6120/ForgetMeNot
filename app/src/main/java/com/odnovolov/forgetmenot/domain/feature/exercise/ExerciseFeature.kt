package com.odnovolov.forgetmenot.domain.feature.exercise

import com.badoo.mvicore.element.*
import com.badoo.mvicore.feature.BaseFeature
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature.*
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature.Action.FulfillWish
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature.Action.ProcessNewExerciseData
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature.Effect.ExerciseCardUpdated
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature.Effect.NewExerciseDataGot
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature.Wish.*
import com.odnovolov.forgetmenot.domain.repository.ExerciseRepository
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

class ExerciseFeature(
    exerciseRepository: ExerciseRepository,
    mainThreadScheduler: Scheduler
) : BaseFeature<Wish, Action, Effect, State, News>(
    initialState = State(),
    wishToAction = { wish -> FulfillWish(wish) },
    bootstrapper = BootstrapperImpl(exerciseRepository, mainThreadScheduler),
    actor = ActorImpl(exerciseRepository, mainThreadScheduler),
    reducer = ReducerImpl(),
    postProcessor = PostProcessorImpl(),
    newsPublisher = NewsPublisherImpl()
) {
    class BootstrapperImpl(
        private val exerciseRepository: ExerciseRepository,
        private val mainThreadScheduler: Scheduler
    ) : Bootstrapper<Action> {
        override fun invoke(): Observable<Action> {
            return exerciseRepository.observeExercise()
                .map { exerciseData: ExerciseData -> ProcessNewExerciseData(exerciseData) as Action }
                .subscribeOn(Schedulers.io())
                .observeOn(mainThreadScheduler)
        }
    }

    sealed class Action {
        data class FulfillWish(val wish: Wish) : Action()
        data class ProcessNewExerciseData(val exerciseData: ExerciseData) : Action()
    }

    sealed class Wish {
        data class ShowAnswer(val position: Int) : Wish()
        data class SetCardAsLearned(val position: Int) : Wish()
        data class SetCardAsUnlearned(val position: Int) : Wish()
    }

    class ActorImpl(
        private val exerciseRepository: ExerciseRepository,
        private val mainThreadScheduler: Scheduler
    ) : Actor<State, Action, Effect> {
        override fun invoke(state: State, action: Action): Observable<Effect> {
            return when (action) {
                is FulfillWish -> when (action.wish) {
                    is ShowAnswer -> {
                        val activeExerciseCard = state.exerciseData.exerciseCards[action.wish.position]
                        Observable.fromCallable { showAnswer(activeExerciseCard) }
                            .map { ExerciseCardUpdated as Effect }
                            .onIo()
                    }
                    is SetCardAsLearned -> {
                        val activeExerciseCard = state.exerciseData.exerciseCards[action.wish.position]
                        Observable.fromCallable { setIsLearned(activeExerciseCard, true) }
                            .map { ExerciseCardUpdated as Effect }
                            .onIo()
                    }
                    is SetCardAsUnlearned -> {
                        val activeExerciseCard = state.exerciseData.exerciseCards[action.wish.position]
                        Observable.fromCallable { setIsLearned(activeExerciseCard, false) }
                            .map { ExerciseCardUpdated as Effect }
                            .onIo()
                    }
                }
                is ProcessNewExerciseData -> {
                    Observable.just(NewExerciseDataGot(action.exerciseData) as Effect)
                }
            }
        }

        private fun showAnswer(exerciseCard: ExerciseCard) {
            val exerciseCardToUpdate = exerciseCard.copy(
                isAnswered = true,
                card = exerciseCard.card.copy(
                    lap = exerciseCard.card.lap + 1
                )
            )
            exerciseRepository.updateExerciseCard(exerciseCardToUpdate)
        }

        private fun setIsLearned(exerciseCard: ExerciseCard, isLearned: Boolean) {
            val exerciseCardToUpdate = exerciseCard.copy(
                card = exerciseCard.card.copy(
                    isLearned = isLearned
                )
            )
            exerciseRepository.updateExerciseCard(exerciseCardToUpdate)
        }

        private fun <T> Observable<T>.onIo(): Observable<T> {
            return this.subscribeOn(Schedulers.io())
                .observeOn(mainThreadScheduler)
        }
    }

    sealed class Effect {
        data class NewExerciseDataGot(val exerciseData: ExerciseData) : Effect()
        object ExerciseCardUpdated : Effect()
    }

    class ReducerImpl : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State {
            return when (effect) {
                is NewExerciseDataGot -> state.copy(exerciseData = effect.exerciseData)
                is ExerciseCardUpdated -> state
            }
        }
    }

    data class State(
        val exerciseData: ExerciseData = ExerciseData()
    )

    class PostProcessorImpl : PostProcessor<Action, Effect, State> {
        override fun invoke(action: Action, effect: Effect, state: State): Action? {
            return null
        }
    }

    class NewsPublisherImpl : NewsPublisher<Action, Effect, State, News> {
        override fun invoke(action: Action, effect: Effect, state: State): News? {
            return null
        }
    }

    sealed class News
}