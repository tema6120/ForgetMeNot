package com.odnovolov.forgetmenot.presentation.screen.dsvformat

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController

class DsvFormatController(
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<DsvFormatEvent, Nothing>() {
    override fun handle(event: DsvFormatEvent) {
        when (event) {

        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}