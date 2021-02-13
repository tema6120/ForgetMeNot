package com.odnovolov.forgetmenot.presentation.screen.fileimport.cardsfile.sourcetext.fileformat

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController

class FileFormatController(
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<FileFormatEvent, Nothing>() {
    override fun handle(event: FileFormatEvent) {
        when (event) {

        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}