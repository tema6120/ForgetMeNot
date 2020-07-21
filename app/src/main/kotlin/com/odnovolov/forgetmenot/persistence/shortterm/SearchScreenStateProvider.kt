package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.shortterm.SearchScreenStateProvider.SerializableState
import com.odnovolov.forgetmenot.presentation.screen.search.SearchScreenState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class SearchScreenStateProvider(
    json: Json,
    database: Database,
    override val key: String = SearchScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<SearchScreenState, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val searchText: String
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: SearchScreenState) = SerializableState(
        state.searchText
    )

    override fun toOriginal(serializableState: SerializableState) = SearchScreenState(
        serializableState.searchText
    )
}