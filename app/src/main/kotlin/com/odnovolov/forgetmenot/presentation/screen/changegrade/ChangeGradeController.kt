package com.odnovolov.forgetmenot.presentation.screen.changegrade

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.changegrade.ChangeGradeEvent.GradeSelected
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorEvent.SelectedGrade

class ChangeGradeController(
    private val dialogState: ChangeGradeDialogState,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<ChangeGradeEvent, Nothing>() {
    override fun handle(event: ChangeGradeEvent) {
        when (event) {
            is GradeSelected -> {
                when (dialogState.caller) {
                    ChangeGradeCaller.DeckEditor -> {
                        DeckEditorDiScope.getOrRecreate().controller
                            .dispatch(SelectedGrade(event.grade))
                    }
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}