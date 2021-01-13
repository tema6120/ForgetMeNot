package com.odnovolov.forgetmenot.presentation.screen.deckeditor.renamedeck

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker

class RenameDeckDialogState(
    typedDeckName: String
) : FlowMaker<RenameDeckDialogState>() {
     var typedDeckName: String by flowMaker(typedDeckName)
 }