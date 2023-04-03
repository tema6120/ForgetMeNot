package com.odnovolov.forgetmenot.presentation.common.mainactivity

import android.content.res.AssetManager
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry
import com.odnovolov.forgetmenot.domain.architecturecomponents.plus
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsImportStorage
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsImporter
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.CardsImporter.ImportResult
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.ImportedCardsFile
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.mainactivity.InitialDecksAdder.Event
import com.odnovolov.forgetmenot.presentation.common.mainactivity.InitialDecksAdder.Event.AppStarted
import java.util.*

class InitialDecksAdder(
    private val state: State,
    private val assetManager: AssetManager,
    private val globalState: GlobalState,
    private val cardsImportStorage: CardsImportStorage,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<Event, Nothing>() {
    class State(areInitialDecksAdded: Boolean = false) : FlowMakerWithRegistry<State>() {
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
                try {
                    addDecksFromAssets()
                } catch (e: Exception) {
                    e.printStackTrace()
                    PropertyChangeRegistry.removeAll()
                    return
                }
                state.areInitialDecksAdded = true
            }
        }
    }

    private fun addDecksFromAssets() {
        val files: List<ImportedCardsFile> = fileNames.map { fileName: String ->
            val fileContent: ByteArray = assetManager.open(fileName).use { it.readBytes() }
            ImportedCardsFile(fileName, fileContent)
        }
        val cardsImporterState = CardsImporter.State.fromFiles(files, cardsImportStorage)
        val cardsImporter = CardsImporter(cardsImporterState, globalState, cardsImportStorage)
        val result: ImportResult = cardsImporter.import()
        when (result) {
            is ImportResult.Failure -> {
                error("Fail to add initial decks from Assets")
            }
            is ImportResult.Success -> {
                val exercisePreferenceForNewDecks: ExercisePreference = createExercisePreference()
                for (deck in result.decks) {
                    deck.exercisePreference = exercisePreferenceForNewDecks
                }
                createDeckListFor(result.decks)
            }
        }
    }

    private fun createExercisePreference(): ExercisePreference {
        val pronunciation = Pronunciation(
            id = generateId(),
            questionLanguage = Locale.ENGLISH,
            questionAutoSpeaking = false,
            answerLanguage = Locale.ENGLISH,
            answerAutoSpeaking = true,
            speakTextInBrackets = false
        )
        val exercisePreference = ExercisePreference(
            id = generateId(),
            name = "English Irregular Verbs",
            randomOrder = true,
            testingMethod = TestingMethod.Manual,
            intervalScheme = IntervalScheme.Default,
            pronunciation = pronunciation,
            isQuestionDisplayed = true,
            cardInversion = CardInversion.Off,
            pronunciationPlan = PronunciationPlan.Default,
            timeForAnswer = DO_NOT_USE_TIMER,
            grading = Grading.Default
        )
        globalState.sharedExercisePreferences =
            globalState.sharedExercisePreferences + exercisePreference
        return exercisePreference
    }

    private fun createDeckListFor(decks: List<Deck>) {
        val deckList = DeckList(
            id = generateId(),
            name = "English irregular verbs",
            color = 0xFFBFBFBF.toInt(),
            deckIds = decks.map { deck -> deck.id }.toSet()
        )
        globalState.deckLists += deckList
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}