package com.odnovolov.forgetmenot.domain.interactor.repetition

import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.checkRepetitionSettingName
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.domain.entity.RepetitionSetting
import com.odnovolov.forgetmenot.domain.entity.RepetitionSetting.Companion
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.domain.isDefault
import com.odnovolov.forgetmenot.domain.isIndividual
import com.soywiz.klock.DateTimeSpan

class RepetitionSettings(
    private val globalState: GlobalState
) {
    private val currentRepetitionSetting: RepetitionSetting
        get() = globalState.currentRepetitionSetting

    fun setCurrentRepetitionSetting(repetitionSettingId: Long) {
        globalState.currentRepetitionSetting = when (repetitionSettingId) {
            globalState.currentRepetitionSetting.id -> return
            RepetitionSetting.Default.id -> RepetitionSetting.Default
            else -> {
                globalState.savedRepetitionSettings
                    .find { it.id == repetitionSettingId }
                    ?: return
            }
        }
    }

    fun createNewSavedRepetitionSetting(name: String) {
        checkName(name)
        createNewSavedRepetitionSettingAndSetAsCurrent(name)
    }

    fun renameRepetitionSetting(repetitionSetting: RepetitionSetting, newName: String) {
        checkName(newName)
        when {
            repetitionSetting.isDefault() -> {
                createNewSavedRepetitionSettingAndSetAsCurrent(newName)
            }
            repetitionSetting.isIndividual() -> {
                repetitionSetting.name = newName
                addNewSavedRepetitionSetting(repetitionSetting)
            }
            else -> {
                repetitionSetting.name = newName
            }
        }
    }

    private fun checkName(testedName: String) {
        when (checkRepetitionSettingName(testedName, globalState)) {
            Ok -> return
            Empty -> throw IllegalArgumentException("saved repetitionSetting name cannot be empty")
            Occupied -> throw IllegalArgumentException("$testedName is occupied")
        }
    }

    private fun createNewSavedRepetitionSettingAndSetAsCurrent(name: String) {
        val newSavedRepetitionSetting: RepetitionSetting = RepetitionSetting.Default
            .shallowCopy(id = generateId(), name = name)
        addNewSavedRepetitionSetting(newSavedRepetitionSetting)
        globalState.currentRepetitionSetting = newSavedRepetitionSetting
    }

    private fun addNewSavedRepetitionSetting(repetitionSetting: RepetitionSetting) {
        globalState.savedRepetitionSettings =
            (globalState.savedRepetitionSettings + repetitionSetting).toCopyableList()
    }

    fun deleteSavedRepetitionSetting(repetitionSettingId: Long) {
        globalState.savedRepetitionSettings = globalState.savedRepetitionSettings
            .filter { it.id != repetitionSettingId }
            .toCopyableList()
        if (globalState.currentRepetitionSetting.id == repetitionSettingId) {
            globalState.currentRepetitionSetting = RepetitionSetting.Default
        }
    }

    fun setIsAvailableForExerciseCardsIncluded(isIncluded: Boolean) {
        updateRepetitionSetting(
            isValueChanged = currentRepetitionSetting
                .isAvailableForExerciseCardsIncluded != isIncluded,
            createNewIndividualRepetitionSetting = {
                currentRepetitionSetting.shallowCopy(
                    id = generateId(),
                    isAvailableForExerciseCardsIncluded = isIncluded
                )
            },
            updateCurrentRepetitionSetting = {
                currentRepetitionSetting.isAvailableForExerciseCardsIncluded = isIncluded
            }
        )
    }

    fun setIsAwaitingCardsIncluded(isIncluded: Boolean) {
        updateRepetitionSetting(
            isValueChanged = currentRepetitionSetting.isAwaitingCardsIncluded != isIncluded,
            createNewIndividualRepetitionSetting = {
                currentRepetitionSetting.shallowCopy(
                    id = generateId(),
                    isAwaitingCardsIncluded = isIncluded
                )
            },
            updateCurrentRepetitionSetting = {
                currentRepetitionSetting.isAwaitingCardsIncluded = isIncluded
            }
        )
    }

    fun setIsLearnedCardsIncluded(isIncluded: Boolean) {
        updateRepetitionSetting(
            isValueChanged = currentRepetitionSetting.isLearnedCardsIncluded != isIncluded,
            createNewIndividualRepetitionSetting = {
                currentRepetitionSetting.shallowCopy(
                    id = generateId(),
                    isLearnedCardsIncluded = isIncluded
                )
            },
            updateCurrentRepetitionSetting = {
                currentRepetitionSetting.isLearnedCardsIncluded = isIncluded
            }
        )
    }

    fun setLevelOfKnowledgeRange(levelOfKnowledgeRange: IntRange) {
        updateRepetitionSetting(
            isValueChanged = currentRepetitionSetting.levelOfKnowledgeRange != levelOfKnowledgeRange,
            createNewIndividualRepetitionSetting = {
                currentRepetitionSetting.shallowCopy(
                    id = generateId(),
                    levelOfKnowledgeRange = levelOfKnowledgeRange
                )
            },
            updateCurrentRepetitionSetting = {
                currentRepetitionSetting.levelOfKnowledgeRange = levelOfKnowledgeRange
            }
        )
    }

    fun setLastAnswerFromTimeAgo(lastAnswerFromTimeAgo: DateTimeSpan?) {
        updateRepetitionSetting(
            isValueChanged = currentRepetitionSetting.lastAnswerFromTimeAgo != lastAnswerFromTimeAgo,
            createNewIndividualRepetitionSetting = {
                currentRepetitionSetting.shallowCopy(
                    id = generateId(),
                    lastAnswerFromTimeAgo = lastAnswerFromTimeAgo
                )
            },
            updateCurrentRepetitionSetting = {
                currentRepetitionSetting.lastAnswerFromTimeAgo = lastAnswerFromTimeAgo
            }
        )
    }

    fun setLastAnswerToTimeAgo(lastAnswerToTimeAgo: DateTimeSpan?) {
        updateRepetitionSetting(
            isValueChanged = currentRepetitionSetting.lastAnswerToTimeAgo != lastAnswerToTimeAgo,
            createNewIndividualRepetitionSetting = {
                currentRepetitionSetting.shallowCopy(
                    id = generateId(),
                    lastAnswerToTimeAgo = lastAnswerToTimeAgo
                )
            },
            updateCurrentRepetitionSetting = {
                currentRepetitionSetting.lastAnswerToTimeAgo = lastAnswerToTimeAgo
            }
        )
    }

    fun setNumberOfLaps(numberOfLaps: Int) {
        if (numberOfLaps <= 0) {
            throw IllegalArgumentException("number of laps should be greater than zero")
        }
        updateRepetitionSetting(
            isValueChanged = currentRepetitionSetting.numberOfLaps != numberOfLaps,
            createNewIndividualRepetitionSetting = {
                currentRepetitionSetting.shallowCopy(
                    id = generateId(),
                    numberOfLaps = numberOfLaps
                )
            },
            updateCurrentRepetitionSetting = {
                currentRepetitionSetting.numberOfLaps = numberOfLaps
            }
        )
    }

    private inline fun updateRepetitionSetting(
        isValueChanged: Boolean,
        createNewIndividualRepetitionSetting: () -> RepetitionSetting,
        updateCurrentRepetitionSetting: () -> Unit
    ) {
        when {
            !isValueChanged -> return
            currentRepetitionSetting.isDefault() -> {
                globalState.currentRepetitionSetting = createNewIndividualRepetitionSetting()
            }
            currentRepetitionSetting.isIndividual() -> {
                updateCurrentRepetitionSetting()
                if (currentRepetitionSetting.shouldBeDefault()) {
                    globalState.currentRepetitionSetting = RepetitionSetting.Default
                }
            }
            else -> {
                updateCurrentRepetitionSetting()
            }
        }
    }

    private fun RepetitionSetting.shallowCopy(
        id: Long,
        name: String = this.name,
        isAvailableForExerciseCardsIncluded: Boolean = this.isAvailableForExerciseCardsIncluded,
        isAwaitingCardsIncluded: Boolean = this.isAwaitingCardsIncluded,
        isLearnedCardsIncluded: Boolean = this.isLearnedCardsIncluded,
        levelOfKnowledgeRange: IntRange = this.levelOfKnowledgeRange,
        lastAnswerFromTimeAgo: DateTimeSpan? = this.lastAnswerFromTimeAgo,
        lastAnswerToTimeAgo: DateTimeSpan? = this.lastAnswerToTimeAgo,
        numberOfLaps: Int = this.numberOfLaps
    ) = RepetitionSetting(
        id,
        name,
        isAvailableForExerciseCardsIncluded,
        isAwaitingCardsIncluded,
        isLearnedCardsIncluded,
        levelOfKnowledgeRange,
        lastAnswerFromTimeAgo,
        lastAnswerToTimeAgo,
        numberOfLaps
    )

    private fun RepetitionSetting.shouldBeDefault(): Boolean =
        this.shallowCopy(id = RepetitionSetting.Default.id) == RepetitionSetting.Default
}