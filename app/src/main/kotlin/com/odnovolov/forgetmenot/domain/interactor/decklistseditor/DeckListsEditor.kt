package com.odnovolov.forgetmenot.domain.interactor.decklistseditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.DeckList
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.generateId

class DeckListsEditor(
    val state: State,
    private val globalState: GlobalState
) {
    class State(
        editingDeckLists: List<DeckList>
    ) : FlowMaker<State>() {
        var editingDeckLists: List<DeckList> by flowMaker(editingDeckLists)

        companion object {
            @OptIn(ExperimentalStdlibApi::class)
            fun create(globalState: GlobalState): State {
                val editingDeckLists: List<DeckList> = buildList {
                    val newDeckList = DeckList(
                        id = generateId(),
                        name = "",
                        color = 0xFF64CFA2.toInt(), // todo
                        deckIds = emptyList()
                    )
                    add(newDeckList)
                    val existingSortedDeckLists = globalState.deckLists.sortedBy { it.name }
                    addAll(existingSortedDeckLists)
                }
                return State(editingDeckLists)
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun createNewDeckList() {
        val newDeckList = DeckList(
            id = generateId(),
            name = "",
            color = 0xFF345FBB.toInt(), // todo
            deckIds = emptyList()
        )
        state.editingDeckLists = buildList {
            add(newDeckList)
            addAll(state.editingDeckLists)
        }
    }

    fun rename(deckListId: Long, newName: String) {
        state.editingDeckLists.find { deckList: DeckList -> deckList.id == deckListId }
            ?.name = newName
    }

    fun save(): SaveResult {
        val newDeckLists = ArrayList<DeckList>()
        for ((position: Int, deckList: DeckList) in state.editingDeckLists.withIndex()) {
            if (position == 0) {
                if (deckList.name.isNotBlank()) {
                    newDeckLists.add(deckList)
                }
            } else {
                if (deckList.name.isBlank()) {
                    return SaveResult.Failure(position)
                }
                newDeckLists.add(deckList)
            }
        }
        globalState.deckLists = newDeckLists.toCopyableList()
        return SaveResult.Success
    }

    sealed class SaveResult {
        object Success : SaveResult()
        class Failure(val position: Int) : SaveResult()
    }
}