package com.odnovolov.forgetmenot.domain.feature.deckspreview

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.element.NewsPublisher
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.BaseFeature
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature.*
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature.Effect.*
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature.Wish.*
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseData
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseCard
import com.odnovolov.forgetmenot.domain.repository.DeckRepository
import com.odnovolov.forgetmenot.domain.repository.ExerciseRepository
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

class DecksPreviewFeature(
        deckRepository: DeckRepository,
        exerciseRepository: ExerciseRepository,
        mainThreadScheduler: Scheduler
) : BaseFeature<Wish, Action, Effect, State, News>(
        initialState = State(),
        wishToAction = { wish -> wish },
        bootstrapper = BootstrapperImpl(deckRepository, mainThreadScheduler),
        actor = ActorImpl(deckRepository, exerciseRepository, mainThreadScheduler),
        reducer = ReducerImpl(),
        newsPublisher = NewsPublisherImpl()
) {
    class BootstrapperImpl(
            private val repository: DeckRepository,
            private val mainThreadScheduler: Scheduler
    ) : Bootstrapper<Action> {
        override fun invoke(): Observable<Action> {
            return repository.observeDecks()
                    .map { decks: List<Deck> -> ProcessNewDecks(decks) as Action }
                    .subscribeOn(Schedulers.io())
                    .observeOn(mainThreadScheduler)
        }
    }

    interface Action
    data class ProcessNewDecks(val decks: List<Deck>) : Action
    sealed class Wish : Action {
        data class PrepareExercise(val deckId: Int) : Wish()
        data class DeleteDeck(val deckId: Int) : Wish()
    }

    class ActorImpl(
            private val deckRepository: DeckRepository,
            private val exerciseRepository: ExerciseRepository,
            private val mainThreadScheduler: Scheduler
    ) : Actor<State, Action, Effect> {
        override fun invoke(state: State, action: Action): Observable<Effect> {
            return when (action) {
                is PrepareExercise -> {
                    Observable.fromCallable { prepareExercise(action.deckId) }
                            .map { ExercisePreparingFinished as Effect }
                            .startWith(ExercisePreparingStarted)
                            .onIo()
                }
                is ProcessNewDecks -> {
                    val decksPreview: List<DeckPreview> = action.decks
                            .map { deck: Deck -> DeckPreview(deck.id, deck.name) }
                            .toList()
                    Observable.just(DeckPreviewUpdated(decksPreview))
                }
                is DeleteDeck -> {
                    Observable.fromCallable { deckRepository.delete(action.deckId) }
                            .map { DeckDeleted as Effect }
                            .onIo()

                }
                else -> Observable.empty()
            }
        }

        private fun prepareExercise(deckId: Int) {
            val deck = deckRepository.getDeck(deckId)
            val exerciseCards: List<ExerciseCard> = deck.cards
                    .map { card -> ExerciseCard(card = card) }
            val exercise = ExerciseData(exerciseCards as MutableList<ExerciseCard>)
            exerciseRepository.deleteAllExercises()
            exerciseRepository.saveExercise(exercise)
        }

        private fun Observable<Effect>.onIo(): Observable<Effect> {
            return this.subscribeOn(Schedulers.io())
                    .observeOn(mainThreadScheduler)
        }
    }

    sealed class Effect {
        data class DeckPreviewUpdated(val decksPreview: List<DeckPreview>) : Effect()
        object DeckDeleted : Effect()
        object ExercisePreparingStarted : Effect()
        object ExercisePreparingFinished : Effect()
    }

    class ReducerImpl : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State {
            return when (effect) {
                is DeckPreviewUpdated -> state.copy(decksPreview = effect.decksPreview)
                is DeckDeleted -> state
                is ExercisePreparingStarted -> state.copy(isExercisePreparing = true)
                is ExercisePreparingFinished -> state.copy(isExercisePreparing = false)
            }
        }
    }

    data class State(
            val decksPreview: List<DeckPreview> = emptyList(),
            val isExercisePreparing: Boolean = false
    )

    class NewsPublisherImpl : NewsPublisher<Action, Effect, State, News> {
        override fun invoke(action: Action, effect: Effect, state: State): News? {
            return when (effect) {
                is ExercisePreparingFinished -> News.ExerciseIsPrepared
                else -> null
            }
        }
    }

    sealed class News {
        object ExerciseIsPrepared : News()
    }
}