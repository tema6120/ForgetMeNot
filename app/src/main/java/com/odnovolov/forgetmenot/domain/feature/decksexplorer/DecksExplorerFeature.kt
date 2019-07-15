package com.odnovolov.forgetmenot.domain.feature.decksexplorer

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.BaseFeature
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.DeckSorting
import com.odnovolov.forgetmenot.domain.entity.DeckSorting.*
import com.odnovolov.forgetmenot.domain.feature.decksexplorer.DecksExplorerFeature.*
import com.odnovolov.forgetmenot.domain.feature.decksexplorer.DecksExplorerFeature.Action.*
import com.odnovolov.forgetmenot.domain.feature.decksexplorer.DecksExplorerFeature.Effect.DeckSortingUpdated
import com.odnovolov.forgetmenot.domain.feature.decksexplorer.DecksExplorerFeature.Effect.DecksUpdated
import com.odnovolov.forgetmenot.domain.feature.decksexplorer.DecksExplorerFeature.Wish.ChangeSorting
import com.odnovolov.forgetmenot.domain.repository.DeckRepository
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

class DecksExplorerFeature(
    deckRepository: DeckRepository,
    mainThreadScheduler: Scheduler
) : BaseFeature<Wish, Action, Effect, State, Nothing>(
    initialState = State(),
    wishToAction = { wish -> FulfillWish(wish) },
    bootstrapper = BootstrapperImpl(deckRepository, mainThreadScheduler),
    actor = ActorImpl(deckRepository, mainThreadScheduler),
    reducer = ReducerImpl()
) {
    class BootstrapperImpl(
        private val repository: DeckRepository,
        private val mainThreadScheduler: Scheduler
    ) : Bootstrapper<Action> {
        override fun invoke(): Observable<Action> {
            return Observable.merge(
                Observable.fromCallable { AcceptSorting(repository.getDeckSorting()) },
                repository.observeDecks()
                    .map { decks: List<Deck> -> AcceptDecks(decks) }
            )
                .subscribeOn(Schedulers.io())
                .observeOn(mainThreadScheduler)
        }
    }

    sealed class Action {
        data class FulfillWish(val wish: Wish) : Action()
        data class AcceptSorting(val deckSorting: DeckSorting?) : Action()
        data class AcceptDecks(val decks: List<Deck>) : Action()
    }

    sealed class Wish {
        data class ChangeSorting(val deckSorting: DeckSorting) : Wish()
    }

    class ActorImpl(
        private val repository: DeckRepository,
        private val mainThreadScheduler: Scheduler
    ) : Actor<State, Action, Effect> {
        override fun invoke(state: State, action: Action): Observable<Effect> {
            return when (action) {
                is FulfillWish -> when (action.wish) {
                    is ChangeSorting -> changeSorting(action.wish.deckSorting, state)
                }
                is AcceptSorting -> acceptSorting(action.deckSorting, state)
                is AcceptDecks -> acceptDecks(action.decks, state)
            }
        }

        private fun changeSorting(deckSorting: DeckSorting, state: State): Observable<Effect> {
            return Observable
                .fromCallable { repository.updateDeckSorting(deckSorting) }
                .map {
                    val sortedDecks = applySorting(state.decks, deckSorting)
                    DeckSortingUpdated(deckSorting, sortedDecks) as Effect
                }
                .schedule()
        }

        private fun acceptSorting(deckSorting: DeckSorting?, state: State): Observable<Effect> {
            val nonNullDeckSorting: DeckSorting = deckSorting ?: BY_NAME
            val sortedDecks = applySorting(state.decks, nonNullDeckSorting)
            return Observable.just(DeckSortingUpdated(nonNullDeckSorting, sortedDecks))
        }

        private fun acceptDecks(decks: List<Deck>, state: State): Observable<Effect> {
            val newDecks =
                if (state.deckSorting == null) {
                    decks
                } else {
                    applySorting(decks, state.deckSorting)
                }
            return Observable.just(DecksUpdated(newDecks))
        }

        private fun applySorting(decks: List<Deck>, sorting: DeckSorting): List<Deck> {
            return when (sorting) {
                BY_NAME -> decks.sortedBy { it.name }
                BY_TIME_CREATED -> decks.sortedBy { it.createdAt }
                BY_LAST_OPENED -> decks.sortedByDescending { it.lastOpenedAt }
            }
        }

        private fun <T> Observable<T>.schedule(): Observable<T> {
            return this
                .subscribeOn(Schedulers.io())
                .observeOn(mainThreadScheduler)
        }
    }

    sealed class Effect {
        data class DeckSortingUpdated(val deckSorting: DeckSorting, val sortedDecks: List<Deck>) : Effect()
        data class DecksUpdated(val decks: List<Deck>) : Effect()
    }

    class ReducerImpl : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State {
            return when (effect) {
                is DeckSortingUpdated -> state.copy(
                    deckSorting = effect.deckSorting,
                    decks = effect.sortedDecks
                )
                is DecksUpdated -> state.copy(
                    decks = effect.decks
                )
            }
        }
    }

    data class State(
        val decks: List<Deck> = emptyList(),
        val deckSorting: DeckSorting? = null
    )
}