package com.odnovolov.forgetmenot.domain.interactor.decksettings

import com.odnovolov.forgetmenot.domain.*
import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings.Event.*
import kotlinx.coroutines.flow.Flow

class DeckSettings(
    val state: State,
    private val globalState: GlobalState
) {
    data class State(val deck: Deck)

    sealed class Event {
        class DeniedExercisePreferenceCreation(val nameCheckResult: NameCheckResult) : Event()
        class DeniedExercisePreferenceRenaming(val nameCheckResult: NameCheckResult) : Event()
    }

    private val eventFlow = EventFlow<Event>()
    val events: Flow<Event> = eventFlow.get()
    private val currentExercisePreference: ExercisePreference
        get() = state.deck.exercisePreference

    fun setExercisePreference(exercisePreferenceId: Long) {
        if (exercisePreferenceId == ExercisePreference.Default.id) {
            setCurrentExercisePreference(ExercisePreference.Default)
        } else {
            globalState.sharedExercisePreferences
                .find { it.id == exercisePreferenceId }
                ?.let(::setCurrentExercisePreference)
        }
    }

    private fun setCurrentExercisePreference(exercisePreference: ExercisePreference) {
        if (state.deck.exercisePreference.id != exercisePreference.id) {
            state.deck.exercisePreference = exercisePreference
        }
    }

    fun createNewSharedExercisePreference(name: String) {
        when (checkExercisePreferenceName(name, globalState)) {
            Ok -> createNewSharedExercisePreferenceAndSetToCurrentDeck(name)
            Empty -> eventFlow.send(DeniedExercisePreferenceCreation(Empty))
            Occupied -> eventFlow.send(DeniedExercisePreferenceCreation(Occupied))
        }
    }

    fun renameExercisePreference(exercisePreference: ExercisePreference, newName: String) {
        when (checkExercisePreferenceName(newName, globalState)) {
            Ok -> {
                when {
                    exercisePreference.isDefault() -> {
                        createNewSharedExercisePreferenceAndSetToCurrentDeck(newName)
                    }
                    exercisePreference.isIndividual() -> {
                        exercisePreference.name = newName
                        addNewSharedExercisePreference(exercisePreference)
                    }
                    else -> { // current ExercisePreference is shared
                        exercisePreference.name = newName
                    }
                }
            }
            Empty -> eventFlow.send(DeniedExercisePreferenceRenaming(Empty))
            Occupied -> eventFlow.send(DeniedExercisePreferenceRenaming(Occupied))
        }
    }

    private fun createNewSharedExercisePreferenceAndSetToCurrentDeck(name: String) {
        val newSharedExercisePreference = ExercisePreference.Default
            .shallowCopy(id = generateId(), name = name)
        addNewSharedExercisePreference(newSharedExercisePreference)
        setCurrentExercisePreference(newSharedExercisePreference)
    }

    private fun addNewSharedExercisePreference(exercisePreference: ExercisePreference) {
        globalState.sharedExercisePreferences =
            (globalState.sharedExercisePreferences + exercisePreference).toCopyableList()
    }

    fun deleteSharedExercisePreference(exercisePreferenceId: Long) {
        if (exercisePreferenceId == ExercisePreference.Default.id) return
        globalState.sharedExercisePreferences = globalState.sharedExercisePreferences
            .filter { it.id != exercisePreferenceId }
            .toCopyableList()
        globalState.decks
            .filter { it.exercisePreference.id == exercisePreferenceId }
            .forEach { it.exercisePreference = ExercisePreference.Default }
    }

    fun setRandomOrder(randomOrder: Boolean) {
        updateExercisePreference(
            isValueChanged = currentExercisePreference.randomOrder != randomOrder,
            createNewIndividualExercisePreference = {
                currentExercisePreference.shallowCopy(
                    id = generateId(),
                    name = "",
                    randomOrder = randomOrder
                )
            },
            updateCurrentExercisePreference = {
                currentExercisePreference.randomOrder = randomOrder
            }
        )
    }

    fun setTestMethod(testMethod: TestMethod) {
        updateExercisePreference(
            isValueChanged = currentExercisePreference.testMethod != testMethod,
            createNewIndividualExercisePreference = {
                currentExercisePreference.shallowCopy(
                    id = generateId(),
                    name = "",
                    testMethod = testMethod
                )
            },
            updateCurrentExercisePreference = {
                currentExercisePreference.testMethod = testMethod
            }
        )
    }

    fun setIntervalScheme(intervalScheme: IntervalScheme?) {
        updateExercisePreference(
            isValueChanged = currentExercisePreference.intervalScheme != intervalScheme,
            createNewIndividualExercisePreference = {
                currentExercisePreference.shallowCopy(
                    id = generateId(),
                    name = "",
                    intervalScheme = intervalScheme
                )
            },
            updateCurrentExercisePreference = {
                currentExercisePreference.intervalScheme = intervalScheme
            }
        )
    }

    fun setPronunciation(pronunciation: Pronunciation) {
        updateExercisePreference(
            isValueChanged = currentExercisePreference.pronunciation != pronunciation,
            createNewIndividualExercisePreference = {
                currentExercisePreference.shallowCopy(
                    id = generateId(),
                    name = "",
                    pronunciation = pronunciation
                )
            },
            updateCurrentExercisePreference = {
                currentExercisePreference.pronunciation = pronunciation
            }
        )
    }

    fun toggleIsQuestionDisplayed() {
        val newIsQuestionDisplayed = !currentExercisePreference.isQuestionDisplayed
        updateExercisePreference(
            isValueChanged = true,
            createNewIndividualExercisePreference = {
                currentExercisePreference.shallowCopy(
                    id = generateId(),
                    name = "",
                    isQuestionDisplayed = newIsQuestionDisplayed
                )
            },
            updateCurrentExercisePreference = {
                currentExercisePreference.isQuestionDisplayed = newIsQuestionDisplayed
            }
        )
    }

    fun setCardInversion(cardInversion: CardInversion) {
        updateExercisePreference(
            isValueChanged = currentExercisePreference.cardInversion != cardInversion,
            createNewIndividualExercisePreference = {
                currentExercisePreference.shallowCopy(
                    id = generateId(),
                    name = "",
                    cardInversion = cardInversion
                )
            },
            updateCurrentExercisePreference = {
                currentExercisePreference.cardInversion = cardInversion
            }
        )
    }

    fun setPronunciationPlan(pronunciationPlan: PronunciationPlan) {
        updateExercisePreference(
            isValueChanged = currentExercisePreference.pronunciationPlan != pronunciationPlan,
            createNewIndividualExercisePreference = {
                currentExercisePreference.shallowCopy(
                    id = generateId(),
                    name = "",
                    pronunciationPlan = pronunciationPlan
                )
            },
            updateCurrentExercisePreference = {
                currentExercisePreference.pronunciationPlan = pronunciationPlan
            }
        )
    }

    fun setTimeForAnswer(timeForAnswer: Int) {
        updateExercisePreference(
            isValueChanged = currentExercisePreference.timeForAnswer != timeForAnswer,
            createNewIndividualExercisePreference = {
                currentExercisePreference.shallowCopy(
                    id = generateId(),
                    name = "",
                    timeForAnswer = timeForAnswer
                )
            },
            updateCurrentExercisePreference = {
                currentExercisePreference.timeForAnswer = timeForAnswer
            }
        )
    }

    fun recheckIndividualExercisePreferences() {
        globalState.decks.forEach { deck: Deck ->
            with(deck) {
                if (exercisePreference.shouldBeDefault()) {
                    exercisePreference = ExercisePreference.Default
                }
            }
        }
    }

    private inline fun updateExercisePreference(
        isValueChanged: Boolean,
        createNewIndividualExercisePreference: () -> ExercisePreference,
        updateCurrentExercisePreference: () -> Unit
    ) {
        when {
            !isValueChanged -> return
            currentExercisePreference.isDefault() -> {
                val newIndividualExercisePreference = createNewIndividualExercisePreference()
                setCurrentExercisePreference(newIndividualExercisePreference)
            }
            currentExercisePreference.isIndividual() -> {
                updateCurrentExercisePreference()
                if (currentExercisePreference.shouldBeDefault()) {
                    setCurrentExercisePreference(ExercisePreference.Default)
                }
            }
            else -> { // current ExercisePreference is shared
                updateCurrentExercisePreference()
            }
        }
    }

    private fun ExercisePreference.shallowCopy(
        id: Long,
        name: String = this.name,
        randomOrder: Boolean = this.randomOrder,
        testMethod: TestMethod = this.testMethod,
        intervalScheme: IntervalScheme? = this.intervalScheme,
        pronunciation: Pronunciation = this.pronunciation,
        isQuestionDisplayed: Boolean = this.isQuestionDisplayed,
        cardInversion: CardInversion = this.cardInversion,
        pronunciationPlan: PronunciationPlan = this.pronunciationPlan,
        timeForAnswer: Int = this.timeForAnswer
    ) = ExercisePreference(
        id,
        name,
        randomOrder,
        testMethod,
        intervalScheme,
        pronunciation,
        isQuestionDisplayed,
        cardInversion,
        pronunciationPlan,
        timeForAnswer
    )

    private fun ExercisePreference.shouldBeDefault(): Boolean {
        return this.shallowCopy(id = ExercisePreference.Default.id) == ExercisePreference.Default
    }
}