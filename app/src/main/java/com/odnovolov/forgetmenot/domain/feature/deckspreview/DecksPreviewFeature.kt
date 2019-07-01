package com.odnovolov.forgetmenot.domain.feature.deckspreview

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.element.NewsPublisher
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.BaseFeature
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.DeckPreview
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature.*
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature.Action.FulfillWish
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature.Action.ProcessNewDecks
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature.Effect.*
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature.Wish.DeleteDeck
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature.Wish.PrepareExercise
import com.odnovolov.forgetmenot.domain.entity.ExerciseCard
import com.odnovolov.forgetmenot.domain.entity.ExerciseData
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
    wishToAction = { wish -> FulfillWish(wish) },
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

    sealed class Action {
        data class FulfillWish(val wish: Wish) : Action()
        data class ProcessNewDecks(val decks: List<Deck>) : Action()
    }

    sealed class Wish {
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
                is FulfillWish -> when (action.wish) {
                    is PrepareExercise -> Observable
                        .fromCallable { prepareExercise(action.wish.deckId) }
                        .map { ExercisePreparingFinished as Effect }
                        .startWith(ExercisePreparingStarted)
                        .onIo()
                    is DeleteDeck -> Observable
                        .fromCallable { deckRepository.delete(action.wish.deckId) }
                        .map { DeckDeleted as Effect }
                        .onIo()
                }
                is ProcessNewDecks -> {
                    val decksPreview: List<DeckPreview> = action.decks
                        .map { deck: Deck ->
                            val passedLaps: Int = deck.cards
                                .filter { !it.isLearned }
                                .map { it.lap }
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
                        .toList()
                    Observable.just(DeckPreviewUpdated(decksPreview))
                }
            }
        }

        private fun prepareExercise(deckId: Int) {
            val deck = deckRepository.getDeck(deckId)
            val exerciseCards: List<ExerciseCard> = deck.cards
                .filter { card -> !card.isLearned }
                .map { card -> ExerciseCard(card = card) }
                .sortedBy { it.card.lap }
            val exercise = ExerciseData(exerciseCards)
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