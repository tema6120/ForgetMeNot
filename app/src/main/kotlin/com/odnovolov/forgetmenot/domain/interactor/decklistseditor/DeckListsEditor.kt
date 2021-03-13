package com.odnovolov.forgetmenot.domain.interactor.decklistseditor

import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.domain.architecturecomponents.toCopyableList
import com.odnovolov.forgetmenot.domain.entity.DeckList
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.generateId
import com.odnovolov.forgetmenot.presentation.screen.decklistseditor.predefinedDeckListColors

class DeckListsEditor(
    val state: State,
    private val globalState: GlobalState
) {
    class State(
        editingDeckLists: List<EditableDeckList>
    ) : FlowMaker<State>() {
        var editingDeckLists: List<EditableDeckList> by flowMaker(editingDeckLists)

        companion object {
            @OptIn(ExperimentalStdlibApi::class)
            fun create(
                globalState: GlobalState,
                deckIdsForNewDeckList: Set<Long> = emptySet()
            ): State {
                val editingDeckLists: List<EditableDeckList> = buildList {
                    val newDeckList = DeckList(
                        id = generateId(),
                        name = "",
                        color = predefinedDeckListColors.random(),
                        deckIds = deckIdsForNewDeckList
                    )
                    val newEditableDeckList = EditableDeckList(newDeckList)
                    add(newEditableDeckList)
                    val existingSortedDeckLists = globalState.deckLists
                        .sortedBy(DeckList::name)
                        .map(::EditableDeckList)
                    addAll(existingSortedDeckLists)
                }
                return State(editingDeckLists)
            }
        }
    }

    private var restoreLastRemovedDeckList: (() -> Unit)? = null

    @OptIn(ExperimentalStdlibApi::class)
    fun createNewDeckList() {
        val newDeckList = DeckList(
            id = generateId(),
            name = "",
            color = predefinedDeckListColors.random(),
            deckIds = emptySet()
        )
        val newEditableDeckList = EditableDeckList(newDeckList)
        state.editingDeckLists = buildList {
            add(newEditableDeckList)
            addAll(state.editingDeckLists)
        }
    }

    fun rename(deckListId: Long, newName: String) {
        state.editingDeckLists.find { editableDeckList: EditableDeckList ->
            editableDeckList.deckList.id == deckListId
        }
            ?.name = newName
    }

    fun remove(deckListId: Long): Boolean {
        val position = state.editingDeckLists.indexOfFirst { editableDeckList: EditableDeckList ->
            editableDeckList.deckList.id == deckListId
        }
        if (position == -1) return false
        val removingEditableDeckList = state.editingDeckLists[position]
        restoreLastRemovedDeckList = {
            state.editingDeckLists = state.editingDeckLists.toMutableList().apply {
                val insertPosition = minOf(position, lastIndex + 1)
                add(insertPosition, removingEditableDeckList)
            }
        }
        state.editingDeckLists = state.editingDeckLists.toMutableList().apply {
            removeAt(position)
        }
        return true
    }

    fun cancelRemoving() {
        restoreLastRemovedDeckList?.invoke()
        restoreLastRemovedDeckList = null
    }

    fun save(): SaveResult {
        check()?.let { failure -> return failure }
        val newDeckLists = ArrayList<DeckList>()
        for ((position: Int, editableDeckList: EditableDeckList) in state.editingDeckLists.withIndex()) {
            if (position == 0) {
                if (editableDeckList.name.isNotBlank()) {
                    editableDeckList.applyChanges()
                    newDeckLists.add(editableDeckList.deckList)
                }
            } else {
                editableDeckList.applyChanges()
                newDeckLists.add(editableDeckList.deckList)
            }
        }
        globalState.deckLists = newDeckLists.toCopyableList()
        return SaveResult.Success
    }

    private fun check(): SaveResult.Failure? {
        return state.editingDeckLists
            .drop(1)
            .find { editableDeckList -> editableDeckList.name.isBlank() }
            ?.let { editableDeckList -> SaveResult.Failure(editableDeckList.deckList.id) }
    }

    private fun EditableDeckList.applyChanges() {
        deckList.name = name
        deckList.color = color
    }

    sealed class SaveResult {
        object Success : SaveResult()
        class Failure(val deckListId: Long) : SaveResult()
    }
}

fun DeckList.addDeckIds(deckIdsToAdd: Collection<Long>) {
    deckIds += deckIdsToAdd
}

fun DeckList.removeDeckIds(deckIdsToRemove: Collection<Long>) {
    deckIds -= deckIdsToRemove
}