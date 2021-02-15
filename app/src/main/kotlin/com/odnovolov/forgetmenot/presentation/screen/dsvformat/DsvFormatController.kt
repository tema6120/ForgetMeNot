package com.odnovolov.forgetmenot.presentation.screen.dsvformat

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.dsvformat.DsvFormatEvent.IgnoreSurroundingSpacesButton

class DsvFormatController(
    private val screenState: DsvFormatScreenState,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<DsvFormatEvent, Nothing>() {
    override fun handle(event: DsvFormatEvent) {
        when (event) {
            is IgnoreSurroundingSpacesButton -> {
                screenState.ignoreSurroundingSpaces = event.ignoreSurroundingSpaces
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}