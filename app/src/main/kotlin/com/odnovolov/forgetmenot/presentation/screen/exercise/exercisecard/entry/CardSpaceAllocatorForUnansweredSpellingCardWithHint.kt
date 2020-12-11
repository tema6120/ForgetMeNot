package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry

import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import com.odnovolov.forgetmenot.presentation.common.dp

object CardSpaceAllocatorForUnansweredSpellingCardWithHint {
    private val CHECK_BUTTON_HEIGHT: Int = 80.dp

    fun allocate(
        availableCardHeight: Int,
        questionFrame: View,
        desiredQuestionFrameHeight: Int,
        hintFrame: View,
        desiredHintFrameHeight: Int,
        inputFrame: View
    ) {
        val maxHeightForQuestionOrHint = (availableCardHeight - CHECK_BUTTON_HEIGHT) / 3
        val minHeightForInputFrame =
            ((availableCardHeight - CHECK_BUTTON_HEIGHT) / 3) + CHECK_BUTTON_HEIGHT
        val doesQuestionFit = desiredQuestionFrameHeight < maxHeightForQuestionOrHint
        val doesHintFit = desiredHintFrameHeight < maxHeightForQuestionOrHint
        val doQuestionAndHintTogether =
            desiredQuestionFrameHeight + desiredHintFrameHeight < maxHeightForQuestionOrHint * 2
        when {
            doQuestionAndHintTogether -> {
                questionFrame.takeAsMuchSpaceAsNecessary()
                hintFrame.takeAsMuchSpaceAsNecessary()
                inputFrame.takeRemainingSpace()
            }
            doesQuestionFit -> {
                questionFrame.takeAsMuchSpaceAsNecessary()
                hintFrame.takeRemainingSpace()
                inputFrame.takeExactSpace(minHeightForInputFrame)
            }
            doesHintFit -> {
                questionFrame.takeRemainingSpace()
                hintFrame.takeAsMuchSpaceAsNecessary()
                inputFrame.takeExactSpace(minHeightForInputFrame)
            }
            else -> {
                questionFrame.takeHalfOfRemainingSpace()
                hintFrame.takeHalfOfRemainingSpace()
                inputFrame.takeExactSpace(minHeightForInputFrame)
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

    private fun View.takeExactSpace(height: Int) {
        updateLayoutParams<LinearLayout.LayoutParams> {
            this.height = height
            weight = 0f
        }
    }

    private fun View.takeHalfOfRemainingSpace() {
        updateLayoutParams<LinearLayout.LayoutParams> {
            height = 0
            weight = 0.5f
        }
    }
}