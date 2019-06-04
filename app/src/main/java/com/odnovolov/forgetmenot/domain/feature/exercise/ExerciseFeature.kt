package com.odnovolov.forgetmenot.domain.feature.exercise

import com.badoo.mvicore.element.*
import com.badoo.mvicore.feature.BaseFeature
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature.*
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature.Action.ProcessNewExerciseData
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature.Effect.GotNewExerciseData
import com.odnovolov.forgetmenot.domain.repository.ExerciseRepository
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

class ExerciseFeature(
    exerciseRepository: ExerciseRepository,
    mainThreadScheduler: Scheduler
) : BaseFeature<Wish, Action, Effect, State, News>(
    initialState = State(),
    wishToAction = { wishAsAction -> wishAsAction },
    bootstrapper = BootstrapperImpl(exerciseRepository, mainThreadScheduler),
    actor = ActorImpl(),
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

    interface Action {
        data class ProcessNewExerciseData(val exerciseData: ExerciseData) : Action
    }

    interface Wish : Action

    class ActorImpl : Actor<State, Action, Effect> {
        override fun invoke(state: State, action: Action): Observable<Effect> {
            return when (action) {
                is ProcessNewExerciseData -> Observable.just(GotNewExerciseData(action.exerciseData) as Effect)
                else -> Observable.empty()
            }
        }
    }

    sealed class Effect {
        data class GotNewExerciseData(val exerciseData: ExerciseData) : Effect()
    }

    class ReducerImpl : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State {
            return when (effect) {
                is GotNewExerciseData -> state.copy(exerciseData = effect.exerciseData)
            }
        }
    }

    data class State(
        val exerciseData: ExerciseData = ExerciseData(emptyList<ExerciseCard>() as MutableList)
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