package com.odnovolov.forgetmenot.domain.feature.decksexplorer

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.BaseFeature
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.feature.decksexplorer.DecksExplorerFeature.*
import com.odnovolov.forgetmenot.domain.feature.decksexplorer.DecksExplorerFeature.Action.FulfillWish
import com.odnovolov.forgetmenot.domain.feature.decksexplorer.DecksExplorerFeature.Action.AcceptNewDecks
import com.odnovolov.forgetmenot.domain.feature.decksexplorer.DecksExplorerFeature.Effect.DecksUpdated
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
    actor = ActorImpl(),
    reducer = ReducerImpl()
) {
    class BootstrapperImpl(
        private val repository: DeckRepository,
        private val mainThreadScheduler: Scheduler
    ) : Bootstrapper<Action> {
        override fun invoke(): Observable<Action> {
            return repository.observeDecks()
                .map { decks: List<Deck> -> AcceptNewDecks(decks) as Action }
                .subscribeOn(Schedulers.io())
                .observeOn(mainThreadScheduler)
        }
    }

    sealed class Action {
        data class FulfillWish(val wish: Wish) : Action()
        data class AcceptNewDecks(val newDecks: List<Deck>) : Action()
    }

    object Wish

    class ActorImpl : Actor<State, Action, Effect> {
        override fun invoke(state: State, action: Action): Observable<Effect> {
            return when (action) {
                is FulfillWish -> Observable.empty()
                is AcceptNewDecks -> Observable.just(DecksUpdated(action.newDecks))
            }
        }
    }

    sealed class Effect {
        data class DecksUpdated(val newDecks: List<Deck>) : Effect()
    }

    class ReducerImpl : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State {
            return when (effect) {
                is DecksUpdated -> State(decks = effect.newDecks)
            }
        }
    }

    data class State(
        val decks: List<Deck> = emptyList()
    )
}