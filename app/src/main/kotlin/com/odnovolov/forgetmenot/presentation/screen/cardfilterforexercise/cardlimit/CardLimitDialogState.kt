package com.odnovolov.forgetmenot.presentation.screen.cardfilterforexercise.cardlimit

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker

class CardLimitDialogState(
    isNoLimit: Boolean,
    dialogText: String
) : FlowMaker<CardLimitDialogState>() {
    var isNoLimit: Boolean by flowMaker(isNoLimit)
    var dialogText: String by flowMaker(dialogText)
}