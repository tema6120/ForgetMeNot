package com.odnovolov.forgetmenot.persistence.shortterm

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.*
import com.odnovolov.forgetmenot.domain.interactor.cardsimport.Parser.Error
import com.odnovolov.forgetmenot.persistence.shortterm.FileImporterStateProvider.SerializableState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.charset.Charset

class FileImporterStateProvider(
    json: Json,
    database: Database,
    private val globalState: GlobalState,
    private val cardsImportStorage: CardsImportStorage,
    override val key: String = CardsImporter.State::class.qualifiedName!!
) : BaseSerializableStateProvider<CardsImporter.State, SerializableState>(
    json,
    database
) {
    @Serializable
    data class SerializableState(
        val files: List<SerializableCardsFile>,
        val currentPosition: Int,
        val maxVisitedPosition: Int
    )

    override val serializer = SerializableState.serializer()

    override fun toSerializable(state: CardsImporter.State): SerializableState {
        val serializableCardsFiles: List<SerializableCardsFile> =
            state.files.map { cardsFile: CardsFile ->
                val serializableAbstractDeck: SerializableAbstractDeck =
                    when (val deckWhereToAdd = cardsFile.deckWhereToAdd) {
                        is NewDeck -> SerializableNewDeck(deckWhereToAdd.deckName)
                        is ExistingDeck -> SerializableExistingDeck(deckWhereToAdd.deck.id)
                        else -> error(ERROR_MESSAGE_UNKNOWN_IMPLEMENTATION_OF_ABSTRACT_DECK)
                    }
                val errors = cardsFile.errors.map { error: Error ->
                    val pair = error.errorRange.first to error.errorRange.last
                    SerializableError(error.message, pair)
                }
                SerializableCardsFile(
                    cardsFile.id,
                    cardsFile.extension,
                    cardsFile.sourceBytes,
                    cardsFile.charset.name(),
                    cardsFile.format.id,
                    cardsFile.text,
                    errors,
                    cardsFile.cardPrototypes,
                    serializableAbstractDeck
                )
            }
        return SerializableState(
            serializableCardsFiles,
            state.currentPosition,
            state.maxVisitedPosition
        )
    }

    override fun toOriginal(serializableState: SerializableState): CardsImporter.State {
        val cardsFiles: List<CardsFile> = serializableState.files
            .map { serializableCardsFile: SerializableCardsFile ->
                val deckWhereToAdd: AbstractDeck =
                    when (val file = serializableCardsFile.serializableAbstractDeck) {
                        is SerializableNewDeck -> NewDeck(file.deckName)
                        is SerializableExistingDeck -> {
                            val deck: Deck = globalState.decks.first { it.id == file.deckId }
                            ExistingDeck(deck)
                        }
                    }
                val format: CardsFileFormat = CardsFileFormat.predefinedFormats
                    .find { it.id == serializableCardsFile.formatId }
                    ?: cardsImportStorage.customFileFormats
                        .first { it.id == serializableCardsFile.formatId }
                val errors: List<Error> = serializableCardsFile.errors
                    .map { serializableError: SerializableError ->
                        val errorRange =
                            serializableError.errorPair.first..serializableError.errorPair.second
                        Error(serializableError.message, errorRange)
                    }
                CardsFile(
                    id = serializableCardsFile.id,
                    extension = serializableCardsFile.extension,
                    sourceBytes = serializableCardsFile.sourceBytes,
                    charset = Charset.forName(serializableCardsFile.charsetName),
                    text = serializableCardsFile.text,
                    format = format,
                    errors = errors,
                    cardPrototypes = serializableCardsFile.cardPrototypes,
                    deckWhereToAdd = deckWhereToAdd
                )
            }
        return CardsImporter.State(
            cardsFiles,
            serializableState.currentPosition,
            serializableState.maxVisitedPosition
        )
    }
}

@Serializable
data class SerializableCardsFile(
    val id: Long,
    val extension: String,
    val sourceBytes: ByteArray,
    val charsetName: String,
    val formatId: Long,
    val text: String,
    val errors: List<SerializableError>,
    val cardPrototypes: List<CardPrototype>,
    val serializableAbstractDeck: SerializableAbstractDeck
)

@Serializable
data class SerializableError(
    val message: String,
    val errorPair: Pair<Int, Int>
)

@Serializable
sealed class SerializableAbstractDeck

@Serializable
class SerializableNewDeck(val deckName: String) : SerializableAbstractDeck()

@Serializable
class SerializableExistingDeck(val deckId: Long) : SerializableAbstractDeck()