package com.odnovolov.forgetmenot.domain.interactor.decklistseditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.entity.DeckList

class EditableDeckList(
    val deckList: DeckList,
    name: String = deckList.name,
    color: Int = deckList.color
) : FlowMaker<EditableDeckList>() {
    var name: String by flowMaker(name)
    var color: Int by flowMaker(color)
}