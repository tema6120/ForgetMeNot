package com.odnovolov.forgetmenot.domain.interactor.repetition

import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.domain.generateId
import com.soywiz.klock.DateTimeSpan

class RepetitionSettings(
    private val globalState: GlobalState
) {
    private val currentRepetitionSetting: RepetitionSetting
        get() = globalState.currentRepetitionSetting

    fun setCurrentRepetitionSetting(repetitionSettingId: Long) {
        globalState.currentRepetitionSetting = when (repetitionSettingId) {
            currentRepetitionSetting.id -> return
            RepetitionSetting.Default.id -> RepetitionSetting.Default
            else -> {
                globalState.sharedRepetitionSettings
                    .find { it.id == repetitionSettingId }
                    ?: return
            }
        }
    }

    fun createNewSharedRepetitionSetting(name: String) {
        checkName(name)
        createNewSharedRepetitionSettingAndSetAsCurrent(name)
    }

    fun renameRepetitionSetting(repetitionSetting: RepetitionSetting, newName: String) {
        checkName(newName)
        when {
            repetitionSetting.isDefault() -> {
                createNewSharedRepetitionSettingAndSetAsCurrent(newName)
            }
            repetitionSetting.isIndividual() -> {
                repetitionSetting.name = newName
                addNewSharedRepetitionSetting(repetitionSetting)
            }
            else -> {
                repetitionSetting.name = newName
            }
        }
    }

    private fun checkName(testedName: String) {
        when (checkRepetitionSettingName(testedName, globalState)) {
            Ok -> return
            Empty -> throw IllegalArgumentException("shared repetitionSetting name cannot be empty")
            Occupied -> throw IllegalArgumentException("$testedName is occupied")
        }
    }

    private fun createNewSharedRepetitionSettingAndSetAsCurrent(name: String) {
        val newSharedRepetitionSetting: RepetitionSetting = RepetitionSetting.Default
            .shallowCopy(id = generateId(), name = name)
        addNewSharedRepetitionSetting(newSharedRepetitionSetting)
        globalState.currentRepetitionSetting = newSharedRepetitionSetting
    }

    private fun addNewSharedRepetitionSetting(repetitionSetting: RepetitionSetting) {
        globalState.sharedRepetitionSettings =
            (globalState.sharedRepetitionSettings + repetitionSetting).toCopyableList()
    }

    fun deleteSharedRepetitionSetting(repetitionSettingId: Long) {
        globalState.sharedRepetitionSettings = globalState.sharedRepetitionSettings
            .filter { it.id != repetitionSettingId }
            .toCopyableList()
        if (globalState.currentRepetitionSetting.id == repetitionSettingId) {
            globalState.currentRepetitionSetting = RepetitionSetting.Default
        }
    }

    fun toggleIsAvailableForExerciseCardsIncluded() {
        val isIncluded = !currentRepetitionSetting.isAvailableForExerciseCardsIncluded
        updateRepetitionSetting(
            isValueChanged = true,
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

    fun toggleIsAwaitingCardsIncluded() {
        val isIncluded = !currentRepetitionSetting.isAwaitingCardsIncluded
        updateRepetitionSetting(
            isValueChanged = true,
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

    fun toggleIsLearnedCardsIncluded() {
        val isIncluded = !currentRepetitionSetting.isLearnedCardsIncluded
        updateRepetitionSetting(
            isValueChanged = true,
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
        require(numberOfLaps > 0) { "number of laps must be greater than zero" }
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