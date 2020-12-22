package com.odnovolov.forgetmenot.presentation.common.mainactivity

import android.content.res.AssetManager
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.plus
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.DeckFromFileCreator
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.DeckFromFileCreator.Result.Success
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.mainactivity.InitialDecksAdder.Event
import com.odnovolov.forgetmenot.presentation.common.mainactivity.InitialDecksAdder.Event.AppStarted
import java.util.*

class InitialDecksAdder(
    private val state: State,
    private val assetManager: AssetManager,
    private val deckFromFileCreator: DeckFromFileCreator,
    private val globalState: GlobalState,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<Event, Nothing>() {
    class State(areInitialDecksAdded: Boolean) : FlowMakerWithRegistry<State>() {
        var areInitialDecksAdded: Boolean by flowMaker(areInitialDecksAdded)

        override fun copy() = State(areInitialDecksAdded)
    }

    sealed class Event {
        object AppStarted : Event()
    }

    private val fileNames: List<String> by lazy {
        listOf(
            "English irregular verbs. Level 7.txt",
            "English irregular verbs. Level 6.txt",
            "English irregular verbs. Level 5.txt",
            "English irregular verbs. Level 4.txt",
            "English irregular verbs. Level 3.txt",
            "English irregular verbs. Level 2.txt",
            "English irregular verbs. Level 1.txt"
        )
    }

    override fun handle(event: Event) {
        when (event) {
            AppStarted -> {
                if (state.areInitialDecksAdded) return
                val exercisePreferenceForNewDecks: ExercisePreference = createExercisePreference()
                try {
                    fileNames.forEach { fileName: String ->
                        val addedDeck: Deck = addDeckFromAssets(fileName)
                        addedDeck.exercisePreference = exercisePreferenceForNewDecks
                    }
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                    PropertyChangeRegistry.removeAll()
                    return
                }
                state.areInitialDecksAdded = true
            }
        }
    }

    private fun createExercisePreference(): ExercisePreference {
        val pronunciation = Pronunciation(
            id = generateId(),
            name = "Only English (_A)",
            questionLanguage = Locale.ENGLISH,
            questionAutoSpeak = false,
            answerLanguage = Locale.ENGLISH,
            answerAutoSpeak = true,
            speakTextInBrackets = false
        )
        globalState.sharedPronunciations = globalState.sharedPronunciations + pronunciation
        val exercisePreference = ExercisePreference(
            id = generateId(),
            name = "For English Irregular Verbs",
            randomOrder = true,
            testMethod = TestMethod.Manual,
            intervalScheme = IntervalScheme.Default,
            pronunciation = pronunciation,
            isQuestionDisplayed = true,
            cardReverse = CardReverse.Off,
            pronunciationPlan = PronunciationPlan.Default,
            timeForAnswer = NOT_TO_USE_TIMER
        )
        globalState.sharedExercisePreferences =
            globalState.sharedExercisePreferences + exercisePreference
        return exercisePreference
    }

    private fun addDeckFromAssets(fileName: String): Deck {
        val inputStream = assetManager.open(fileName)
        val result = deckFromFileCreator.loadFromFile(inputStream, fileName)
        if (result !is Success) {
            throw IllegalStateException(
                "Fail to add initial deck ($fileName) from Assets - $result"
            )
        }
        return result.deck
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}