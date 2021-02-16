package com.odnovolov.forgetmenot.presentation.screen.dsvformat

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker

class DsvFormatScreenState(
    purpose: Purpose,
    isTipVisible: Boolean = true
) : FlowMaker<DsvFormatScreenState>() {
    val purpose: Purpose by flowMaker(purpose)
    var isTipVisible: Boolean by flowMaker(isTipVisible)

    enum class Purpose {
        View,
        EditExisting,
        CreateNew
    }
}