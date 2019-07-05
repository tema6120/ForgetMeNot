package com.odnovolov.forgetmenot.domain.feature.exercisecreator

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.NewsPublisher
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.ActorReducerFeature
import com.odnovolov.forgetmenot.domain.entity.ExerciseCard
import com.odnovolov.forgetmenot.domain.entity.ExerciseData
import com.odnovolov.forgetmenot.domain.feature.exercisecreator.ExerciseCreatorFeature.*
import com.odnovolov.forgetmenot.domain.feature.exercisecreator.ExerciseCreatorFeature.Effect.CreationFinished
import com.odnovolov.forgetmenot.domain.feature.exercisecreator.ExerciseCreatorFeature.Effect.CreationStarted
import com.odnovolov.forgetmenot.domain.feature.exercisecreator.ExerciseCreatorFeature.News.ExerciseCreated
import com.odnovolov.forgetmenot.domain.feature.exercisecreator.ExerciseCreatorFeature.Wish.CreateExercise
import com.odnovolov.forgetmenot.domain.repository.DeckRepository
import com.odnovolov.forgetmenot.domain.repository.ExerciseRepository
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

class ExerciseCreatorFeature(
    deckRepository: DeckRepository,
    exerciseRepository: ExerciseRepository,
    mainThreadScheduler: Scheduler
) : ActorReducerFeature<Wish, Effect, State, News>(
    initialState = State(),
    actor = ActorImpl(deckRepository, exerciseRepository, mainThreadScheduler),
    reducer = ReducerImpl(),
    newsPublisher = NewsPublisherImpl()
) {
    sealed class Wish {
        data class CreateExercise(val deckId: Int) : Wish()
    }

    class ActorImpl(
        private val deckRepository: DeckRepository,
        private val exerciseRepository: ExerciseRepository,
        private val mainThreadScheduler: Scheduler
    ) : Actor<State, Wish, Effect> {
        override fun invoke(state: State, wish: Wish): Observable<Effect> = when (wish) {
            is CreateExercise -> createExercise(wish.deckId)
        }

        private fun createExercise(deckId: Int): Observable<Effect> {
            return Observable.fromCallable {
                val deck = deckRepository.getDeck(deckId)
                val exerciseCards: List<ExerciseCard> = deck.cards
                    .filter { card -> !card.isLearned }
                    .map { card -> ExerciseCard(card = card) }
                    .sortedBy { exerciseCard -> exerciseCard.card.lap }
                val exercise = ExerciseData(exerciseCards)
                exerciseRepository.deleteAllExercises()
                exerciseRepository.saveExercise(exercise)
            }
                .map { CreationFinished as Effect }
                .startWith(CreationStarted)
                .schedule()
        }

        private fun <T> Observable<T>.schedule(): Observable<T> {
            return this.subscribeOn(Schedulers.io())
                .observeOn(mainThreadScheduler)
        }
    }

    sealed class Effect {
        object CreationStarted : Effect()
        object CreationFinished : Effect()
    }

    class ReducerImpl : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State = when (effect) {
            CreationStarted -> State(isProcessing = true)
            CreationFinished -> State(isProcessing = false)
        }
    }

    data class State(
        val isProcessing: Boolean = false
    )

    class NewsPublisherImpl : NewsPublisher<Wish, Effect, State, News> {
        override fun invoke(wish: Wish, effect: Effect, state: State): News? = when(effect) {
            CreationFinished -> ExerciseCreated
            else -> null
        }
    }

    sealed class News {
        object ExerciseCreated : News()
    }
}