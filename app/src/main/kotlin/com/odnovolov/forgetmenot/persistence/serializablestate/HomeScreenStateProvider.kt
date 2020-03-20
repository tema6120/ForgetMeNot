package com.odnovolov.forgetmenot.persistence.serializablestate

import com.odnovolov.forgetmenot.persistence.serializablestate.HomeScreenStateProvider.SerializableHomeScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenState
import kotlinx.serialization.Serializable

class HomeScreenStateProvider
    : BaseSerializableStateProvider<HomeScreenState, SerializableHomeScreenState>() {
    @Serializable
    data class SerializableHomeScreenState(
        val searchText: String,
        val selectedDeckIds: List<Long>
    )

    override val serializer = SerializableHomeScreenState.serializer()
    override val serializableClassName = SerializableHomeScreenState::class.java.name
    override val defaultState = HomeScreenState()

    override fun toSerializable(state: HomeScreenState) = SerializableHomeScreenState(
        state.searchText,
        state.selectedDeckIds
    )

    override fun toOriginal(serializableState: SerializableHomeScreenState) =
        HomeScreenState().apply {
            searchText = serializableState.searchText
            selectedDeckIds = serializableState.selectedDeckIds
        }
}