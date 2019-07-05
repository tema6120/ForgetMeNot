package com.odnovolov.forgetmenot.domain.feature.adddeck

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.NewsPublisher
import com.badoo.mvicore.element.PostProcessor
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.BaseFeature
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.feature.adddeck.AddDeckFeature.*
import com.odnovolov.forgetmenot.domain.feature.adddeck.AddDeckFeature.Action.*
import com.odnovolov.forgetmenot.domain.feature.adddeck.AddDeckFeature.Effect.*
import com.odnovolov.forgetmenot.domain.feature.adddeck.AddDeckFeature.State.Stage.*
import com.odnovolov.forgetmenot.domain.feature.adddeck.AddDeckFeature.Wish.*
import com.odnovolov.forgetmenot.domain.feature.adddeck.AddDeckFeature.Wish.Cancel
import com.odnovolov.forgetmenot.domain.repository.DeckRepository
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.io.InputStream
import java.nio.charset.Charset

class AddDeckFeature(
    repository: DeckRepository,
    mainThreadScheduler: Scheduler
) : BaseFeature<Wish, Action, Effect, State, News>(
    initialState = State(),
    wishToAction = { wish -> FulfillWish(wish) },
    actor = ActorImpl(repository, mainThreadScheduler),
    reducer = ReducerImpl(),
    postProcessor = PostProcessorImpl(),
    newsPublisher = NewsPublisherImpl()
) {
    sealed class Action {
        data class FulfillWish(val wish: Wish) : Action()
        object CheckName : Action()
        object Save : Action()
    }

    sealed class Wish {
        data class AddFromInputStream(
            val inputStream: InputStream,
            val charset: Charset = Charset.defaultCharset(),
            val fileName: String = ""
        ) : Wish()

        data class OfferName(val offeredName: String) : Wish()
        object Cancel : Wish()
    }

    class ActorImpl(
        private val repository: DeckRepository,
        private val mainThreadScheduler: Scheduler
    ) : Actor<State, Action, Effect> {
        override fun invoke(state: State, action: Action): Observable<Effect> = when (action) {
            is FulfillWish -> when (action.wish) {
                is AddFromInputStream -> addFromInputStream(
                    action.wish.inputStream,
                    action.wish.charset,
                    action.wish.fileName
                )
                is OfferName -> checkName(action.wish.offeredName)
                Cancel -> Observable.just(Cancelled as Effect)
            }
            CheckName -> checkName(state.deck!!.name)
            Save -> save(state.deck!!)
        }

        private fun addFromInputStream(
            inputStream: InputStream,
            charset: Charset,
            fileName: String
        ): Observable<Effect> {
            return Observable.fromCallable { Parser.parse(inputStream, charset) }
                .map { cards: List<Card> -> Deck(name = fileName, cards = cards) }
                .map { deck: Deck -> ParsingFinishedWithSuccess(deck) as Effect }
                .startWith(ParsingStarted)
                .onErrorReturn { throwable: Throwable -> ParsingFinishedWishError(throwable) }
                .schedule()
        }

        private fun checkName(offeredName: String): Observable<Effect> {
            return Observable.fromCallable {
                when {
                    offeredName.isEmpty() -> NameIsEmpty
                    repository.getAllDeckNames().any { it == offeredName } -> NameIsOccupied(offeredName)
                    else -> NameIsOkay(offeredName)
                }
            }
                .schedule()
        }

        private fun save(deck: Deck): Observable<Effect> {
            return Observable.fromCallable {
                val deckId = repository.saveDeck(deck)
                repository.saveLastInsertedDeckId(deckId)
            }
                .map { SavingCompleted as Effect }
                .startWith(SavingStarted)
                .schedule()
        }

        private fun <T> Observable<T>.schedule(): Observable<T> {
            return this
                .subscribeOn(Schedulers.io())
                .observeOn(mainThreadScheduler)
        }
    }

    sealed class Effect {
        object ParsingStarted : Effect()
        data class ParsingFinishedWithSuccess(val deck: Deck) : Effect()
        data class ParsingFinishedWishError(val throwable: Throwable) : Effect()
        object NameIsEmpty : Effect()
        data class NameIsOccupied(val occupiedName: String) : Effect()
        data class NameIsOkay(val name: String) : Effect()
        object Cancelled : Effect()
        object SavingStarted : Effect()
        object SavingCompleted : Effect()
    }

    class ReducerImpl : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State {
            return when (effect) {
                is ParsingStarted -> state.copy(
                    stage = Parsing
                )
                is ParsingFinishedWithSuccess -> state.copy(
                    stage = Idle,
                    deck = effect.deck
                )
                is ParsingFinishedWishError -> State()
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
                is Cancelled -> State()
                is SavingStarted -> state.copy(
                    stage = Saving
                )
                is SavingCompleted -> State()
            }
        }
    }

    data class State(
        val stage: Stage = Idle,
        val deck: Deck? = null
    ) {
        sealed class Stage {
            object Idle : Stage()
            object Parsing : Stage()
            object WaitingForName : Stage()
            data class WaitingForChangingName(val occupiedName: String) : Stage()
            object Saving : Stage()
        }
    }

    class PostProcessorImpl : PostProcessor<Action, Effect, State> {
        override fun invoke(action: Action, effect: Effect, state: State): Action? {
            return when (effect) {
                is ParsingFinishedWithSuccess -> CheckName
                is NameIsOkay -> Save
                else -> null
            }
        }
    }

    class NewsPublisherImpl : NewsPublisher<Action, Effect, State, News?> {
        override fun invoke(action: Action, effect: Effect, state: State): News? {
            return when (effect) {
                is ParsingFinishedWishError -> News.ErrorHappened(effect.throwable.message ?: "")
                is SavingCompleted -> News.DeckAdded
                else -> null
            }
        }
    }

    sealed class News {
        data class ErrorHappened(val message: String) : News()
        object DeckAdded : News()
    }
}