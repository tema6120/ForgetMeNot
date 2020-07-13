package com.odnovolov.forgetmenot.presentation.screen.ongoingcardeditor

import com.odnovolov.forgetmenot.domain.interactor.cardeditor.EditableCard
import com.odnovolov.forgetmenot.domain.interactor.cardeditor.OngoingCardEditor
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.ShortTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.base.BaseController
import com.odnovolov.forgetmenot.presentation.common.catchAndLogException
import com.odnovolov.forgetmenot.presentation.screen.ongoingcardeditor.OngoingCardEditorController.Command
import com.odnovolov.forgetmenot.presentation.screen.ongoingcardeditor.OngoingCardEditorController.Command.AskUserToConfirmExit
import com.odnovolov.forgetmenot.presentation.screen.ongoingcardeditor.OngoingCardEditorEvent.*

class OngoingCardEditorController(
    private val ongoingCardEditor: OngoingCardEditor,
    private val navigator: Navigator,
    private val longTermStateSaver: LongTermStateSaver,
    private val ongoingCardEditorStateProvider: ShortTermStateProvider<EditableCard>
) : BaseController<OngoingCardEditorEvent, Command>() {
    sealed class Command {
        object AskUserToConfirmExit : Command()
    }

    override fun handle(event: OngoingCardEditorEvent) {
        when (event) {
            CancelButtonClicked -> {
                navigator.navigateUp()
            }

            AcceptButtonClicked -> {
                catchAndLogException {
                    ongoingCardEditor.save()
                    navigator.navigateUp()
                }
            }

            BackButtonClicked -> {
                if (ongoingCardEditor.isCardEdited()) {
                    sendCommand(AskUserToConfirmExit)
                } else {
                    navigator.navigateUp()
                }
            }

            UserConfirmedExit -> {
                navigator.navigateUp()
            }
        }
    }

    override fun saveState() {
        longTermStateSaver.saveStateByRegistry()
        ongoingCardEditorStateProvider.save(ongoingCardEditor.editableCard)
    }
}