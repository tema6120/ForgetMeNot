package com.odnovolov.forgetmenot.presentation.screen.changegrade

import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.screen.changegrade.ChangeGradeEvent.GradeSelected
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
            is GradeSelected -> {
                when (dialogState.caller) {
                    ChangeGradeCaller.DeckEditor -> {
                        DeckEditorDiScope.getOrRecreate().controller
                            .dispatch(DeckEditorEvent.SelectedGrade(event.grade))
                    }
                    ChangeGradeCaller.Search -> {
                        SearchDiScope.getOrRecreate().controller
                            .dispatch(SearchEvent.SelectedGrade(event.grade))
                    }
                    ChangeGradeCaller.HomeSearch -> {
                        HomeDiScope.getOrRecreate().controller
                            .dispatch(HomeEvent.SelectedGrade(event.grade))
                    }
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
    }
}