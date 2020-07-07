package com.odnovolov.forgetmenot.presentation.screen.cardseditor

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.CardsEditor
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.catchAndLogException
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorController.Command
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorController.Command.MoveToPosition
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorEvent.*

class CardsEditorController(
    private val cardsEditor: CardsEditor,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val cardsEditorStateProvider: ShortTermStateProvider<CardsEditor.State>
) : BaseController<CardsEditorEvent, Command>() {
    sealed class Command {
        class MoveToPosition(val position: Int) : Command()
    }

    override fun handle(event: CardsEditorEvent) {
        when (event) {
            is PageSelected -> {
                cardsEditor.setCurrentPosition(event.position)
            }

            CancelButtonClicked -> {
                navigator.navigateUp()
            }

            AcceptButtonClicked -> {
                catchAndLogException {
                    val success: Boolean = cardsEditor.applyChanges()
                    if (success) {
                        navigator.navigateUp()
                    } else {
                        val firstUnderfilledCardPosition = cardsEditor.state.editableCards
                            .indexOfFirst(cardsEditor::isCardUnderfilled)
                        sendCommand(MoveToPosition(firstUnderfilledCardPosition))
                    }
                }
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        cardsEditorStateProvider.save(cardsEditor.state)
    }
}