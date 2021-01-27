package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerScreenState
import kotlinx.serialization.json.Json

class PlayerScreenStateProvider(
    json: Json,
    database: Database,
    override val key: String = PlayerScreenState::class.qualifiedName!!
) : BaseSerializableStateProvider<PlayerScreenState, PlayerScreenState>(
    json,
    database
) {
    override val serializer = PlayerScreenState.serializer()

    override fun toSerializable(state: PlayerScreenState) = state

    override fun toOriginal(serializableState: PlayerScreenState) = serializableState
}