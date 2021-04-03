package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry

class Grading(
    override val id: Long,
    onFirstCorrectAnswer: GradeChangeOnCorrectAnswer,
    onFirstWrongAnswer: GradeChangeOnWrongAnswer,
    askAgain: Boolean,
    onRepeatedCorrectAnswer: GradeChangeOnCorrectAnswer,
    onRepeatedWrongAnswer: GradeChangeOnWrongAnswer
) : FlowMakerWithRegistry<Grading>() {
    var onFirstCorrectAnswer: GradeChangeOnCorrectAnswer by flowMaker(onFirstCorrectAnswer)
    var onFirstWrongAnswer: GradeChangeOnWrongAnswer by flowMaker(onFirstWrongAnswer)
    var askAgain: Boolean by flowMaker(askAgain)
    var onRepeatedCorrectAnswer: GradeChangeOnCorrectAnswer by flowMaker(onRepeatedCorrectAnswer)
    var onRepeatedWrongAnswer: GradeChangeOnWrongAnswer by flowMaker(onRepeatedWrongAnswer)

    override fun copy() = Grading(
        id,
        onFirstCorrectAnswer,
        onFirstWrongAnswer,
        askAgain,
        onRepeatedCorrectAnswer,
        onRepeatedWrongAnswer
    )

    companion object {
        val Default by lazy {
            Grading(
                id = 0L,
                onFirstCorrectAnswer = GradeChangeOnCorrectAnswer.PlusOne,
                onFirstWrongAnswer = GradeChangeOnWrongAnswer.MinusOne,
                askAgain = true,
                onRepeatedCorrectAnswer = GradeChangeOnCorrectAnswer.DoNotChange,
                onRepeatedWrongAnswer = GradeChangeOnWrongAnswer.MinusOne
            )
        }
    }
}

fun Grading.isDefault(): Boolean = id == Grading.Default.id

interface GradeChange {
    fun apply(gradeBeforeAnswer: Int): Int
}

enum class GradeChangeOnCorrectAnswer : GradeChange {
    DoNotChange {
        override fun apply(gradeBeforeAnswer: Int) = gradeBeforeAnswer
    },
    PlusOne {
        override fun apply(gradeBeforeAnswer: Int) = gradeBeforeAnswer + 1
    },
    PlusTwo {
        override fun apply(gradeBeforeAnswer: Int) = gradeBeforeAnswer + 2
    }
}

enum class GradeChangeOnWrongAnswer : GradeChange {
    DoNotChange {
        override fun apply(gradeBeforeAnswer: Int) = gradeBeforeAnswer
    },
    MinusOne {
        override fun apply(gradeBeforeAnswer: Int) = maxOf(0, gradeBeforeAnswer - 1)
    },
    MinusTwo {
        override fun apply(gradeBeforeAnswer: Int) = maxOf(0, gradeBeforeAnswer - 2)
    },
    ResetToZero {
        override fun apply(gradeBeforeAnswer: Int) = 0
    }
}