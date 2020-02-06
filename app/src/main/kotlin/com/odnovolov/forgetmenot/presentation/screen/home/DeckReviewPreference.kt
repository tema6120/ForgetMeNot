package com.odnovolov.forgetmenot.presentation.screen.home

import com.odnovolov.forgetmenot.domain.architecturecomponents.RegistrableFlowableState

class DeckReviewPreference(
    displayOnlyWithTasks: Boolean = false
) : RegistrableFlowableState<DeckReviewPreference>() {
    var displayOnlyWithTasks: Boolean by me(displayOnlyWithTasks)

    override fun copy() = DeckReviewPreference(displayOnlyWithTasks)
}