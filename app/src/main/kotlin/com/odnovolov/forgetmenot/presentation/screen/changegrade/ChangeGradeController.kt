package com.odnovolov.forgetmenot.presentation.screen.changegrade

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.changegrade.ChangeGradeEvent.GradeWasSelected
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorEvent
import com.odnovolov.forgetmenot.presentation.screen.home.HomeDiScope
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent
import com.odnovolov.forgetmenot.presentation.screen.search.SearchDiScope
import com.odnovolov.forgetmenot.presentation.screen.search.SearchEvent

class ChangeGradeController(
    private val dialogState: ChangeGradeDialogState,
    private val longTermStateSaver: LongTermStateSaver
) : BaseController<ChangeGradeEvent, Nothing>() {
    override fun handle(event: ChangeGradeEvent) {
        when (event) {
            is GradeWasSelected -> {
                when (dialogState.caller) {
                    ChangeGradeCaller.DeckEditor -> {
                        DeckEditorDiScope.getOrRecreate().controller
                            .dispatch(DeckEditorEvent.GradeWasSelected(event.grade))
                    }
                    ChangeGradeCaller.Search -> {
                        SearchDiScope.getOrRecreate().controller
                            .dispatch(SearchEvent.GradeWasSelected(event.grade))
                    }
                    ChangeGradeCaller.HomeSearch -> {
                        HomeDiScope.getOrRecreate().controller
                            .dispatch(HomeEvent.GradeWasSelected(event.grade))
                    }
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}