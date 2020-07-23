package com.odnovolov.forgetmenot.domain.interactor.cardeditor

import com.odnovolov.forgetmenot.domain.interactor.exercise.Exercise

class CardsEditorForExercise(
    private val exercise: Exercise,
    removedCards: MutableList<EditableCard> = ArrayList(),
    state: State
) : CardsEditorForEditingSpecificCards(
    removedCards,
    state
)