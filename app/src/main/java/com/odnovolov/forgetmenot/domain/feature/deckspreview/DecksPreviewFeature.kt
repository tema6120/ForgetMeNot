package com.odnovolov.forgetmenot.domain.feature.deckspreview

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.BaseFeature
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.DeckPreview
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature.*
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature.Action.FulfillWish
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature.Action.ProcessNewDecks
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature.Effect.DeckPreviewUpdated
import com.odnovolov.forgetmenot.domain.repository.DeckRepository
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers

class DecksPreviewFeature(
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
                .map { decks: List<Deck> -> ProcessNewDecks(decks) as Action }
                .subscribeOn(Schedulers.io())
                .observeOn(mainThreadScheduler)
        }
    }

    sealed class Action {
        data class FulfillWish(val wish: Wish) : Action()
        data class ProcessNewDecks(val decks: List<Deck>) : Action()
    }

    object Wish

    class ActorImpl : Actor<State, Action, Effect> {
        override fun invoke(state: State, action: Action): Observable<Effect> {
            return when (action) {
                is FulfillWish -> Observable.empty()
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
    }

    sealed class Effect {
        data class DeckPreviewUpdated(val decksPreview: List<DeckPreview>) : Effect()
    }

    class ReducerImpl : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State {
            return when (effect) {
                is DeckPreviewUpdated -> state.copy(decksPreview = effect.decksPreview)
            }
        }
    }

    data class State(
        val decksPreview: List<DeckPreview> = emptyList()
    )
}