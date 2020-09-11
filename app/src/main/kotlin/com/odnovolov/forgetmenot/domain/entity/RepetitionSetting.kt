package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.RegistrableFlowableState
import com.soywiz.klock.DateTimeSpan

class RepetitionSetting(
    override val id: Long,
    name: String,
    isAvailableForExerciseCardsIncluded: Boolean,
    isAwaitingCardsIncluded: Boolean,
    isLearnedCardsIncluded: Boolean,
    levelOfKnowledgeRange: IntRange,
    lastAnswerFromTimeAgo: DateTimeSpan?,
    lastAnswerToTimeAgo: DateTimeSpan?,
    numberOfLaps: Int
) : RegistrableFlowableState<RepetitionSetting>() {
    var name: String by me(name)
    var isAvailableForExerciseCardsIncluded: Boolean by me(isAvailableForExerciseCardsIncluded)
    var isAwaitingCardsIncluded: Boolean by me(isAwaitingCardsIncluded)
    var isLearnedCardsIncluded: Boolean by me(isLearnedCardsIncluded)
    var levelOfKnowledgeRange: IntRange by me(levelOfKnowledgeRange)
    var lastAnswerFromTimeAgo: DateTimeSpan? by me(lastAnswerFromTimeAgo) // null means zero time
    var lastAnswerToTimeAgo: DateTimeSpan? by me(lastAnswerToTimeAgo) // null means now
    var numberOfLaps: Int by me(numberOfLaps)

    override fun copy() = RepetitionSetting(
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

    companion object {
        val Default by lazy {
            val maxLevelOfKnowledge: Int = IntervalScheme.Default.intervals.last().levelOfKnowledge
            RepetitionSetting(
                id = 0L,
                name = "",
                isAvailableForExerciseCardsIncluded = false,
                isAwaitingCardsIncluded = true,
                isLearnedCardsIncluded = false,
                levelOfKnowledgeRange = 0..maxLevelOfKnowledge,
                lastAnswerFromTimeAgo = null,
                lastAnswerToTimeAgo = null,
                numberOfLaps = 1
            )
        }
    }
}

fun RepetitionSetting.isDefault(): Boolean = this.id == RepetitionSetting.Default.id

fun RepetitionSetting.isIndividual(): Boolean = !isDefault() && name.isEmpty()

fun checkRepetitionSettingName(testingName: String, globalState: GlobalState): NameCheckResult {
    return when {
        testingName.isEmpty() -> NameCheckResult.Empty
        globalState.sharedRepetitionSettings.any { it.name == testingName } ->
            NameCheckResult.Occupied
        else -> NameCheckResult.Ok
    }
}