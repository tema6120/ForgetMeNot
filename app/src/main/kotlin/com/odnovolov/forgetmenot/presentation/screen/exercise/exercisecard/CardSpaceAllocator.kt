package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard

import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams

object CardSpaceAllocator {
    fun allocate(
        availableCardHeight: Int,
        questionFrame: View,
        desiredQuestionFrameHeight: Int,
        answerFrame: View,
        desiredAnswerFrameHeight: Int
    ) {
        val half: Int = availableCardHeight / 2
        val doesQuestionFit: Boolean = desiredQuestionFrameHeight < half
        val doesAnswerFit: Boolean = desiredAnswerFrameHeight < half
        val doQuestionAndAnswerFitTogether: Boolean =
            desiredQuestionFrameHeight + desiredAnswerFrameHeight < availableCardHeight
        when {
            doesQuestionFit && doesAnswerFit -> {
                questionFrame.takeHalfSpace()
                answerFrame.takeHalfSpace()
            }
            doesQuestionFit && doQuestionAndAnswerFitTogether -> {
                questionFrame.takeRemainingSpace()
                answerFrame.takeAsMuchSpaceAsNecessary()
            }
            doesAnswerFit && doQuestionAndAnswerFitTogether -> {
                questionFrame.takeAsMuchSpaceAsNecessary()
                answerFrame.takeRemainingSpace()
            }
            doesQuestionFit -> {
                questionFrame.takeAsMuchSpaceAsNecessary()
                answerFrame.takeRemainingSpace()
            }
            doesAnswerFit -> {
                questionFrame.takeRemainingSpace()
                answerFrame.takeAsMuchSpaceAsNecessary()
            }
            else -> {
                questionFrame.takeHalfSpace()
                answerFrame.takeHalfSpace()
            }
        }
    }

    private fun View.takeAsMuchSpaceAsNecessary() {
        updateLayoutParams<LinearLayout.LayoutParams> {
            height = WRAP_CONTENT
            weight = 0f
        }
    }

    private fun View.takeRemainingSpace() {
        updateLayoutParams<LinearLayout.LayoutParams> {
            height = 0
            weight = 1f
        }
    }

    private fun View.takeHalfSpace() {
        updateLayoutParams<LinearLayout.LayoutParams> {
            height = 0
            weight = 0.5f
        }
    }
}