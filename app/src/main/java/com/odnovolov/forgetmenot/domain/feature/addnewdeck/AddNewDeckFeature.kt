package com.odnovolov.forgetmenot.domain.feature.addnewdeck

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.NewsPublisher
import com.badoo.mvicore.element.PostProcessor
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.BaseFeature
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature.*
import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature.Effect.*
import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature.State.Stage.*
import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature.Wish.*
import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature.Wish.Cancel
import com.odnovolov.forgetmenot.domain.repository.DeckRepository
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.io.InputStream
import java.nio.charset.Charset

class AddNewDeckFeature(
    repository: DeckRepository,
    mainThreadScheduler: Scheduler
) : BaseFeature<Wish, Action, Effect, State, News>(
    initialState = State(),
    wishToAction = { wish -> wish },
    actor = ActorImpl(repository, mainThreadScheduler),
    reducer = ReducerImpl(),
    postProcessor = PostProcessorImpl(),
    newsPublisher = NewsPublisherImpl()
) {
    data class State(
        val stage: Stage = Idle,
        val deck: Deck? = null
    ) {
        sealed class Stage {
            object Idle : Stage()
            object Processing : Stage()
            object WaitingForName : Stage()
            data class WaitingForChangingName(val occupiedName: String) : Stage()
            object Saving : Stage()
        }
    }

    sealed class News {
        data class ErrorHappened(val message: String) : News()
        object DeckAdded : News()
    }

    sealed class Wish : Action {
        data class AddFromInputStream(
            val inputStream: InputStream,
            val charset: Charset = Charset.defaultCharset(),
            val fileName: String = ""
        ) : Wish()

        data class OfferName(val offeredName: String) : Wish()
        object Cancel : Wish()
    }

    interface Action
    data class SaveDeck(val deck: Deck) : Action

    sealed class Effect {
        object ParsingStarted : Effect()
        data class SuccessfulParsing(val deck: Deck) : Effect()
        data class ErrorParsing(val throwable: Throwable) : Effect()
        object NameIsEmpty : Effect()
        data class NameIsOccupied(val occupiedName: String) : Effect()
        data class NameIsOkay(val name: String) : Effect()
        object Cancel : Effect()
        object SavingStarted : Effect()
        object SavingCompleted : Effect()
    }

    class ActorImpl(
        private val repository: DeckRepository,
        private val mainThreadScheduler: Scheduler
    ) : Actor<State, Action, Effect> {
        override fun invoke(state: State, action: Action): Observable<Effect> {
            return when (action) {
                is AddFromInputStream -> {
                    Observable.fromCallable { Parser.parse(action.inputStream, action.charset) }
                        .map { cards: List<Card> -> Deck(name = action.fileName, cards = cards) }
                        .map { deck: Deck -> SuccessfulParsing(deck) as Effect }
                        .startWith(Effect.ParsingStarted)
                        .onErrorReturn { throwable: Throwable -> Effect.ErrorParsing(throwable) }
                        .onIo()
                }
                is OfferName -> Observable.fromCallable { checkName(action.offeredName) }.onIo()
                is Cancel -> Observable.just(Effect.Cancel)
                is SaveDeck -> {
                    Observable.fromCallable { saveDeck(state.deck!!) }
                        .map { SavingCompleted as Effect }
                        .startWith(Effect.SavingStarted)
                        .onIo()
                }
                else -> Observable.empty()
            }
        }

        private fun checkName(testedName: String): Effect {
            return when {
                testedName.isEmpty() -> Effect.NameIsEmpty
                repository.getAllDeckNames().any { it == testedName } -> Effect.NameIsOccupied(testedName)
                else -> Effect.NameIsOkay(testedName)
            }
        }

        private fun saveDeck(deck: Deck) {
            val deckId = repository.insertDeck(deck)
            repository.saveDeckIdAsLastInserted(deckId)
        }

        private fun Observable<Effect>.onIo() =
            this.subscribeOn(Schedulers.io())
                .observeOn(mainThreadScheduler)
    }

    class ReducerImpl : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State {
            return when (effect) {
                is ParsingStarted -> state.copy(
                    stage = Processing
                )
                is SuccessfulParsing -> state.copy(
                    stage = Idle,
                    deck = effect.deck
                )
                is ErrorParsing -> State()
                is NameIsEmpty -> state.copy(
                    stage = WaitingForName
                )
                is NameIsOccupied -> state.copy(
                    stage = WaitingForChangingName(effect.occupiedName)
                )
                is NameIsOkay -> state.copy(
                    stage = Idle,
                    deck = state.deck!!.copy(name = effect.name)
                )
                is Effect.Cancel -> State()
                is SavingStarted -> state.copy(
                    stage = Saving
                )
                is SavingCompleted -> State()
            }
        }
    }

    class PostProcessorImpl : PostProcessor<Action, Effect, State> {
        override fun invoke(action: Action, effect: Effect, state: State): Action? {
            return when (effect) {
                is SuccessfulParsing -> OfferName(state.deck!!.name)
                is NameIsOkay -> SaveDeck(state.deck!!)
                else -> null
            }
        }
    }

    class NewsPublisherImpl : NewsPublisher<Action, Effect, State, News?> {
        override fun invoke(action: Action, effect: Effect, state: State): News? {
            return when (effect) {
                is ErrorParsing -> News.ErrorHappened(effect.throwable.message ?: "")
                is SavingCompleted -> News.DeckAdded
                else -> null
            }
        }
    }
}