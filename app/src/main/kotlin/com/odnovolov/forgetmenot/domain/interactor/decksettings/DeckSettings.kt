package com.odnovolov.forgetmenot.domain.interactor.decksettings

import com.odnovolov.forgetmenot.domain.*
import com.odnovolov.forgetmenot.domain.architecturecomponents.EventFlow
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowableState
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings.Event.*
import kotlinx.coroutines.flow.Flow

class DeckSettings(
    val state: State,
    private val globalState: GlobalState
) {
    class State(
        deck: Deck
    ) : FlowableState<State>() {
        var deck: Deck by me(deck)
    }

    sealed class Event {
        class DeniedDeckRenaming(val nameCheckResult: NameCheckResult) : Event()
        class DeniedExercisePreferenceCreation(val nameCheckResult: NameCheckResult) : Event()
        class DeniedExercisePreferenceRenaming(val nameCheckResult: NameCheckResult) : Event()
    }

    private val eventFlow = EventFlow<Event>()
    val events: Flow<Event> = eventFlow.get()
    val currentExercisePreference: ExercisePreference
        get() = state.deck.exercisePreference

    fun renameDeck(newName: String) {
        when (checkDeckName(newName, globalState)) {
            Ok -> state.deck.name = newName
            Empty -> eventFlow.send(DeniedDeckRenaming(Empty))
            Occupied -> eventFlow.send(DeniedDeckRenaming(Occupied))
        }
    }

    fun setExercisePreference(exercisePreferenceId: Long) {
        if (exercisePreferenceId == ExercisePreference.Default.id) {
            setExercisePreference(ExercisePreference.Default)
        } else {
            globalState.sharedExercisePreferences
                .find { it.id == exercisePreferenceId }
                ?.let(::setExercisePreference)
        }
    }

    private fun setExercisePreference(exercisePreference: ExercisePreference) {
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
        setExercisePreference(newSharedExercisePreference)
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
                    pronunciation = pronunciation
                )
            },
            updateCurrentExercisePreference = {
                currentExercisePreference.pronunciation = pronunciation
            }
        )
    }

    fun setIsQuestionDisplayed(isQuestionDisplayed: Boolean) {
        updateExercisePreference(
            isValueChanged = currentExercisePreference.isQuestionDisplayed != isQuestionDisplayed,
            createNewIndividualExercisePreference = {
                currentExercisePreference.shallowCopy(
                    id = generateId(),
                    isQuestionDisplayed = isQuestionDisplayed
                )
            },
            updateCurrentExercisePreference = {
                currentExercisePreference.isQuestionDisplayed = isQuestionDisplayed
            }
        )
    }

    fun setCardReverse(cardReverse: CardReverse) {
        updateExercisePreference(
            isValueChanged = currentExercisePreference.cardReverse != cardReverse,
            createNewIndividualExercisePreference = {
                currentExercisePreference.shallowCopy(
                    id = generateId(),
                    cardReverse = cardReverse
                )
            },
            updateCurrentExercisePreference = {
                currentExercisePreference.cardReverse = cardReverse
            }
        )
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
                setExercisePreference(newIndividualExercisePreference)
            }
            currentExercisePreference.isIndividual() -> {
                updateCurrentExercisePreference()
                if (currentExercisePreference.shouldBeDefault()) {
                    setExercisePreference(ExercisePreference.Default)
                }
            }
            else -> { // current ExercisePreference is shared
                updateCurrentExercisePreference()
            }
        }
    }
}