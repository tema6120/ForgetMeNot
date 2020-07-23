package com.odnovolov.forgetmenot.presentation.screen.ongoingcardeditor

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.OngoingCardEditor
import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise
import com.odnovolov.forgetmenot.persistence.shortterm.OngoingCardEditorStateProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor.QAEditorViewModel
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseDiScope

class OngoingCardEditorDiScope private constructor(
    initialEditableCard: EditableCard? = null
) {
    private val ongoingCardEditorStateProvider = OngoingCardEditorStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    private val editableCard: EditableCard =
        initialEditableCard ?: ongoingCardEditorStateProvider.load()

    private val exercise: Exercise?
        get() = if (ExerciseDiScope.isOpen()) {
            ExerciseDiScope.shareExercise()
        } else {
            null
        }

    private val ongoingCardEditor = OngoingCardEditor(
        editableCard,
        exercise
    )

    val controller = OngoingCardEditorController(
        ongoingCardEditor,
        AppDiScope.get().navigator,
        AppDiScope.get().longTermStateSaver,
        ongoingCardEditorStateProvider
    )

    val viewModel = OngoingCardEditorViewModel(
        editableCard
    )

    val qaEditorViewModel = QAEditorViewModel(
        editableCard
    )

    companion object : DiScopeManager<OngoingCardEditorDiScope>() {
        fun create(initialEditableCard: EditableCard) =
            OngoingCardEditorDiScope(initialEditableCard)

        override fun recreateDiScope() = OngoingCardEditorDiScope()

        override fun onCloseDiScope(diScope: OngoingCardEditorDiScope) {
            diScope.controller.dispose()
        }
    }
}