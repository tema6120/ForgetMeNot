package com.odnovolov.forgetmenot.domain.interactor.decksettings

import com.odnovolov.forgetmenot.domain.*
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.*

class DeckSettings(
    val state: State,
    private val globalState: GlobalState
) {
    data class State(val deck: Deck)

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

    fun createNewSharedExercisePreference(name: String): NameCheckResult {
        return checkExercisePreferenceName(name, globalState)
            .also { nameCheckResult: NameCheckResult ->
                if (nameCheckResult == NameCheckResult.Ok) {
                    createNewSharedExercisePreferenceAndSetToCurrentDeck(name)
                }
            }
    }

    fun renameExercisePreference(
        exercisePreference: ExercisePreference,
        newName: String
    ): NameCheckResult {
        return checkExercisePreferenceName(newName, globalState)
            .also { nameCheckResult: NameCheckResult ->
                if (nameCheckResult != NameCheckResult.Ok) return@also
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
    }

    private fun createNewSharedExercisePreferenceAndSetToCurrentDeck(name: String) {
        val newSharedExercisePreference = currentExercisePreference.shallowCopy(
            id = generateId(),
            name = name,
            intervalScheme = currentExercisePreference.intervalScheme?.copyWithNewId(),
            pronunciation = currentExercisePreference.pronunciation.copyWithNewId(),
            grading = currentExercisePreference.grading.copyWithNewId(),
            pronunciationPlan = currentExercisePreference.pronunciationPlan.copyWithNewId()
        )
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

    fun setTestingMethod(testingMethod: TestingMethod) {
        updateExercisePreference(
            isValueChanged = currentExercisePreference.testingMethod != testingMethod,
            createNewIndividualExercisePreference = {
                currentExercisePreference.shallowCopy(
                    id = generateId(),
                    name = "",
                    testingMethod = testingMethod
                )
            },
            updateCurrentExercisePreference = {
                currentExercisePreference.testingMethod = testingMethod
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

    fun setGrading(grading: Grading) {
        updateExercisePreference(
            isValueChanged = currentExercisePreference.grading != grading,
            createNewIndividualExercisePreference = {
                currentExercisePreference.shallowCopy(
                    id = generateId(),
                    name = "",
                    grading = grading
                )
            },
            updateCurrentExercisePreference = {
                currentExercisePreference.grading = grading
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
        pronunciation: Pronunciation = this.pronunciation,
        cardInversion: CardInversion = this.cardInversion,
        isQuestionDisplayed: Boolean = this.isQuestionDisplayed,
        testingMethod: TestingMethod = this.testingMethod,
        intervalScheme: IntervalScheme? = this.intervalScheme,
        grading: Grading = this.grading,
        timeForAnswer: Int = this.timeForAnswer,
        pronunciationPlan: PronunciationPlan = this.pronunciationPlan
    ) = ExercisePreference(
        id,
        name,
        randomOrder,
        pronunciation,
        cardInversion,
        isQuestionDisplayed,
        testingMethod,
        intervalScheme,
        grading,
        timeForAnswer,
        pronunciationPlan
    )

    private fun ExercisePreference.shouldBeDefault(): Boolean {
        return this.shallowCopy(id = ExercisePreference.Default.id) == ExercisePreference.Default
    }

    private fun Pronunciation.copyWithNewId() = Pronunciation(
        id = generateId(),
        questionLanguage,
        questionAutoSpeaking,
        answerLanguage,
        answerAutoSpeaking,
        speakTextInBrackets
    )

    private fun IntervalScheme.copyWithNewId() = IntervalScheme(
        id = generateId(),
        intervals = intervals.map { interval: Interval ->
            Interval(
                id = generateId(),
                grade = interval.grade,
                value = interval.value
            )
        }.toCopyableList()
    )

    private fun Grading.copyWithNewId() = Grading(
        id = generateId(),
        onFirstCorrectAnswer,
        onFirstWrongAnswer,
        askAgain,
        onRepeatedCorrectAnswer,
        onRepeatedWrongAnswer
    )

    private fun PronunciationPlan.copyWithNewId() = PronunciationPlan(
        id = generateId(),
        pronunciationEvents
    )
}