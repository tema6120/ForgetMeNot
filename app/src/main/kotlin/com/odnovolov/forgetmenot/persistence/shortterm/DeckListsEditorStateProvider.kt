package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.DeckList
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.DeckListsEditor
import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.EditableDeckList
import com.odnovolov.forgetmenot.persistence.shortterm.DeckListsEditorStateProvider.SerializableState
import com.odnovolov.forgetmenot.persistence.shortterm.SerializableEditableDeckList.BrandNewSEDeckList
import com.odnovolov.forgetmenot.persistence.shortterm.SerializableEditableDeckList.ExistingSEDeckList
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DeckListsEditorStateProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    override val key: String = DeckListsEditor.State::class.qualifiedName!!
) : BaseSerializableStateProvider<DeckListsEditor.State, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val editingDeckLists: List<SerializableEditableDeckList>
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: DeckListsEditor.State): SerializableState {
        val existingDeckListIds: List<Long> = globalState.deckLists.map { it.id }
        val serializableEditableDeckLists: List<SerializableEditableDeckList> =
            state.editingDeckLists.map { editableDeckList: EditableDeckList ->
                if (editableDeckList.deckList.id in existingDeckListIds) {
                    ExistingSEDeckList(
                        editableDeckList.deckList.id,
                        editableDeckList.name,
                        editableDeckList.color
                    )
                } else {
                    BrandNewSEDeckList(
                        editableDeckList.deckList.id,
                        editableDeckList.name,
                        editableDeckList.color,
                        editableDeckList.deckList.deckIds
                    )
                }
            }
        return SerializableState(serializableEditableDeckLists)
    }

    override fun toOriginal(serializableState: SerializableState): DeckListsEditor.State {
        val deckListIdDeckListMap: Map<Long, DeckList> =
            globalState.deckLists.associateBy { deckList: DeckList -> deckList.id }
        val editingDeckLists: List<EditableDeckList> = serializableState.editingDeckLists
            .map { serializableEditableDeckList: SerializableEditableDeckList ->
                when (serializableEditableDeckList) {
                    is ExistingSEDeckList -> {
                        val deckList: DeckList =
                            deckListIdDeckListMap.getValue(serializableEditableDeckList.deckListId)
                        EditableDeckList(
                            deckList,
                            serializableEditableDeckList.name,
                            serializableEditableDeckList.color
                        )
                    }
                    is BrandNewSEDeckList -> {
                        val deckList = DeckList(
                            serializableEditableDeckList.deckListId,
                            serializableEditableDeckList.name,
                            serializableEditableDeckList.color,
                            serializableEditableDeckList.deckIds
                        )
                        EditableDeckList(
                            deckList,
                            serializableEditableDeckList.name,
                            serializableEditableDeckList.color
                        )
                    }
                }
            }
        return DeckListsEditor.State(editingDeckLists)
    }
}

@Serializable
sealed class SerializableEditableDeckList {
    @Serializable
    data class ExistingSEDeckList(
        val deckListId: Long,
        val name: String,
        val color: Int
    ) : SerializableEditableDeckList()

    @Serializable
    data class BrandNewSEDeckList(
        val deckListId: Long,
        val name: String,
        val color: Int,
        val deckIds: Set<Long>
    ) : SerializableEditableDeckList()
}