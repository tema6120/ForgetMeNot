package com.odnovolov.forgetmenot.domain.entity

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMakerWithRegistry

class DeckList(
    override val id: Long,
    name: String,
    color: Int,
    deckIds: Set<Long>
) : FlowMakerWithRegistry<DeckList>() {
    var name: String by flowMaker(name)
    var color: Int by flowMaker(color)
    var deckIds: Set<Long> by flowMakerForSet(deckIds)

    override fun copy() = DeckList(id, name, color, deckIds)
}