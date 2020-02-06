package com.odnovolov.forgetmenot.persistence.serializablestate

import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenState
import kotlinx.serialization.Serializable

object HomeScreenStateProvider {
    fun load(): HomeScreenState {
        return loadSerializable(SerializableHomeScreenState.serializer())
            ?.toOriginal()
            ?: HomeScreenState()
    }

    fun save(state: HomeScreenState) {
        val serializable = state.toSerializable()
        saveSerializable(serializable, SerializableHomeScreenState.serializer())
    }

    fun delete() {
        deleteSerializable(SerializableHomeScreenState::class)
    }

    @Serializable
    private data class SerializableHomeScreenState(
        val searchText: String,
        val selectedDeckIds: List<Long>
    )

    private fun HomeScreenState.toSerializable() = SerializableHomeScreenState(
        searchText,
        selectedDeckIds
    )

    private fun SerializableHomeScreenState.toOriginal() = HomeScreenState().apply {
        searchText = this@toOriginal.searchText
        selectedDeckIds = this@toOriginal.selectedDeckIds
    }
}