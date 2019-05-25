package com.odnovolov.forgetmenot.domain.feature.deckspreview

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.BaseFeature
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature.*
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature.Effect.DeckDeleted
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature.Effect.DeckPreviewUpdated
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature.Wish.DeleteDeck
import com.odnovolov.forgetmenot.domain.repository.DeckRepository
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

class DecksPreviewFeature(
    repository: DeckRepository,
    mainThreadScheduler: Scheduler
) : BaseFeature<Wish, Action, Effect, State, Nothing>(
    initialState = State(),
    wishToAction = { wish -> wish },
    bootstrapper = BootstrapperImpl(repository, mainThreadScheduler),
    actor = ActorImpl(repository, mainThreadScheduler),
    reducer = ReducerImpl()
) {
    data class State(
        val decksPreview: List<DeckPreview> = emptyList()
    )

    sealed class Wish : Action {
        data class DeleteDeck(val deckId: Int) : Wish()
    }

    interface Action
    data class ProcessNewDecks(val decks: List<Deck>) : Action

    sealed class Effect {
        data class DeckPreviewUpdated(val decksPreview: List<DeckPreview>) : Effect()
        object DeckDeleted : Effect()
    }

    class BootstrapperImpl(
        private val repository: DeckRepository,
        private val mainThreadScheduler: Scheduler
    ) : Bootstrapper<Action> {
        override fun invoke(): Observable<Action> {
            return repository.loadAll()
                .map { decks: List<Deck> -> ProcessNewDecks(decks) as Action }
                .subscribeOn(Schedulers.io())
                .observeOn(mainThreadScheduler)
        }
    }

    class ActorImpl(
        private val repository: DeckRepository,
        private val mainThreadScheduler: Scheduler
    ) : Actor<State, Action, Effect> {
        override fun invoke(state: State, action: Action): Observable<Effect> {
            return when (action) {
                is ProcessNewDecks -> {
                    val decksPreview: List<DeckPreview> = action.decks
                        .map { deck: Deck -> DeckPreview(deck.id, deck.name) }
                        .toList()
                    Observable.just(DeckPreviewUpdated(decksPreview))
                }
                is DeleteDeck -> {
                    Observable.fromCallable { repository.delete(action.deckId) }
                        .map { DeckDeleted as Effect }
                        .onIo()

                }
                else -> Observable.empty()
            }
        }

        private fun Observable<Effect>.onIo(): Observable<Effect> {
            return this.subscribeOn(Schedulers.io())
                .observeOn(mainThreadScheduler)
        }
    }

    class ReducerImpl : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State {
            return when (effect) {
                is DeckPreviewUpdated -> State(effect.decksPreview)
                is DeckDeleted -> state
            }
        }
    }
}