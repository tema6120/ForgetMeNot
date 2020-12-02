package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry
import com.soywiz.klock.DateTimeSpan

class RepetitionSetting(
    override val id: Long,
    name: String,
    isAvailableForExerciseCardsIncluded: Boolean,
    isAwaitingCardsIncluded: Boolean,
    isLearnedCardsIncluded: Boolean,
    gradeRange: IntRange,
    lastAnswerFromTimeAgo: DateTimeSpan?,
    lastAnswerToTimeAgo: DateTimeSpan?,
    numberOfLaps: Int
) : FlowMakerWithRegistry<RepetitionSetting>() {
    var name: String by flowMaker(name)
    var isAvailableForExerciseCardsIncluded: Boolean by flowMaker(isAvailableForExerciseCardsIncluded)
    var isAwaitingCardsIncluded: Boolean by flowMaker(isAwaitingCardsIncluded)
    var isLearnedCardsIncluded: Boolean by flowMaker(isLearnedCardsIncluded)
    var gradeRange: IntRange by flowMaker(gradeRange)
    var lastAnswerFromTimeAgo: DateTimeSpan? by flowMaker(lastAnswerFromTimeAgo) // null means zero time
    var lastAnswerToTimeAgo: DateTimeSpan? by flowMaker(lastAnswerToTimeAgo) // null means now
    var numberOfLaps: Int by flowMaker(numberOfLaps)

    override fun copy() = RepetitionSetting(
        id,
        name,
        isAvailableForExerciseCardsIncluded,
        isAwaitingCardsIncluded,
        isLearnedCardsIncluded,
        gradeRange,
        lastAnswerFromTimeAgo,
        lastAnswerToTimeAgo,
        numberOfLaps
    )

    companion object {
        val Default by lazy {
            val maxGrade: Int = IntervalScheme.Default.intervals.last().grade
            RepetitionSetting(
                id = 0L,
                name = "",
                isAvailableForExerciseCardsIncluded = false,
                isAwaitingCardsIncluded = true,
                isLearnedCardsIncluded = false,
                gradeRange = 0..maxGrade,
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