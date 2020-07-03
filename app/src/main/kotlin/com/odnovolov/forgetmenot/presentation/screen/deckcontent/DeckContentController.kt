package com.odnovolov.forgetmenot.presentation.screen.deckcontent

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.deckcontent.DeckContentController.Command

class DeckContentController(
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<DeckContentEvent, Command>() {
    sealed class Command {
    }

    override fun handle(event: DeckContentEvent) {
        when (event) {
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}