package com.odnovolov.forgetmenot.domain.interactor.decksettings

import com.odnovolov.forgetmenot.domain.entity.GradeChangeOnCorrectAnswer
import com.odnovolov.forgetmenot.domain.entity.GradeChangeOnWrongAnswer
import com.odnovolov.forgetmenot.domain.entity.Grading
import com.odnovolov.forgetmenot.domain.entity.isDefault
import com.odnovolov.forgetmenot.domain.generateId

class GradingSettings(
    private val deckSettings: DeckSettings
) {
    private val currentGrading: Grading
        get() = deckSettings.state.deck.exercisePreference.grading

    fun setOnFirstCorrectAnswer(onFirstCorrectAnswer: GradeChangeOnCorrectAnswer) {
        updateGrading(
            isValueChanged = currentGrading.onFirstCorrectAnswer != onFirstCorrectAnswer,
            createNewGrading = {
                currentGrading.shallowCopy(
                    id = generateId(),
                    onFirstCorrectAnswer = onFirstCorrectAnswer
                )
            },
            updateCurrentGrading = {
                currentGrading.onFirstCorrectAnswer = onFirstCorrectAnswer
            }
        )
    }

    fun setOnFirstWrongAnswer(onFirstWrongAnswer: GradeChangeOnWrongAnswer) {
        updateGrading(
            isValueChanged = currentGrading.onFirstWrongAnswer != onFirstWrongAnswer,
            createNewGrading = {
                currentGrading.shallowCopy(
                    id = generateId(),
                    onFirstWrongAnswer = onFirstWrongAnswer
                )
            },
            updateCurrentGrading = {
                currentGrading.onFirstWrongAnswer = onFirstWrongAnswer
            }
        )
    }

    fun setAskAgain(askAgain: Boolean) {
        updateGrading(
            isValueChanged = currentGrading.askAgain != askAgain,
            createNewGrading = {
                currentGrading.shallowCopy(
                    id = generateId(),
                    askAgain = askAgain
                )
            },
            updateCurrentGrading = {
                currentGrading.askAgain = askAgain
            }
        )
    }

    fun setOnRepeatedCorrectAnswer(onRepeatedCorrectAnswer: GradeChangeOnCorrectAnswer) {
        updateGrading(
            isValueChanged = currentGrading.onRepeatedCorrectAnswer != onRepeatedCorrectAnswer,
            createNewGrading = {
                currentGrading.shallowCopy(
                    id = generateId(),
                    onRepeatedCorrectAnswer = onRepeatedCorrectAnswer
                )
            },
            updateCurrentGrading = {
                currentGrading.onRepeatedCorrectAnswer = onRepeatedCorrectAnswer
            }
        )
    }

    fun setOnRepeatedWrongAnswer(onRepeatedWrongAnswer: GradeChangeOnWrongAnswer) {
        updateGrading(
            isValueChanged = currentGrading.onRepeatedWrongAnswer != onRepeatedWrongAnswer,
            createNewGrading = {
                currentGrading.shallowCopy(
                    id = generateId(),
                    onRepeatedWrongAnswer = onRepeatedWrongAnswer
                )
            },
            updateCurrentGrading = {
                currentGrading.onRepeatedWrongAnswer = onRepeatedWrongAnswer
            }
        )
    }

    private inline fun updateGrading(
        isValueChanged: Boolean,
        crossinline createNewGrading: () -> Grading,
        crossinline updateCurrentGrading: () -> Unit
    ) {
        when {
            !isValueChanged -> return
            currentGrading.isDefault() -> {
                val newGrading = createNewGrading()
                deckSettings.setGrading(newGrading)
            }
            else -> {
                updateCurrentGrading()
                if (currentGrading.shouldBeDefault()) {
                    deckSettings.setGrading(Grading.Default)
                }
            }
        }
    }

    private fun Grading.shallowCopy(
        id: Long,
        onFirstCorrectAnswer: GradeChangeOnCorrectAnswer = this.onFirstCorrectAnswer,
        onFirstWrongAnswer: GradeChangeOnWrongAnswer = this.onFirstWrongAnswer,
        askAgain: Boolean = this.askAgain,
        onRepeatedCorrectAnswer: GradeChangeOnCorrectAnswer = this.onRepeatedCorrectAnswer,
        onRepeatedWrongAnswer: GradeChangeOnWrongAnswer = this.onRepeatedWrongAnswer,
    ) = Grading(
        id,
        onFirstCorrectAnswer,
        onFirstWrongAnswer,
        askAgain,
        onRepeatedCorrectAnswer,
        onRepeatedWrongAnswer
    )

    private fun Grading.shouldBeDefault(): Boolean =
        this.shallowCopy(id = Grading.Default.id) == Grading.Default
}