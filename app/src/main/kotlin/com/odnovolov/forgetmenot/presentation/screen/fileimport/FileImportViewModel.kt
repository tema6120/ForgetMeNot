package com.odnovolov.forgetmenot.presentation.screen.fileimport

import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.interactor.deckeditor.checkDeckName
import com.odnovolov.forgetmenot.domain.interactor.fileimport.*
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import kotlinx.coroutines.flow.*
import java.nio.charset.Charset

class FileImportViewModel(
    private val fileImporterState: FileImporter.State,
    private val globalState: GlobalState
) {
    val deckName: Flow<String> = fileImporterState.files[0].flowOf(CardsFile::deckWhereToAdd)
        .flatMapLatest { deckWhereToAdd: AbstractDeck ->
            when (deckWhereToAdd) {
                is NewDeck -> deckWhereToAdd.flowOf(NewDeck::deckName)
                is ExistingDeck -> deckWhereToAdd.deck.flowOf(Deck::name)
                else -> error(ERROR_MESSAGE_UNKNOWN_IMPLEMENTATION_OF_ABSTRACT_DECK)
            }
        }

    val deckNameCheckResult: Flow<NameCheckResult> = fileImporterState.files[0]
        .flowOf(CardsFile::deckWhereToAdd).flatMapLatest { abstractDeck: AbstractDeck ->
            when (abstractDeck) {
                is NewDeck -> {
                    abstractDeck.flowOf(NewDeck::deckName)
                        .map { deckName: String -> checkDeckName(deckName, globalState) }
                }
                is ExistingDeck -> flowOf(NameCheckResult.Ok)
                else -> error(ERROR_MESSAGE_UNKNOWN_IMPLEMENTATION_OF_ABSTRACT_DECK)
            }
        }
        .flowOn(businessLogicThread)

    val isNewDeck: Flow<Boolean> = fileImporterState.files[0]
        .flowOf(CardsFile::deckWhereToAdd)
        .map { deckWhereToAdd: AbstractDeck -> deckWhereToAdd is NewDeck }

    val currentCharset: Flow<Charset> = fileImporterState.files[0].flowOf(CardsFile::charset)

    @OptIn(ExperimentalStdlibApi::class)
    val availableCharsets: Flow<List<CharsetItem>> = currentCharset.map { currentCharset: Charset ->
        buildList<Charset> {
            addAll(mainCharsets)
            for (charset in Charset.availableCharsets().values) {
                if (charset !in mainCharsets) {
                    add(charset)
                }
            }
        }.map { charset: Charset ->
            CharsetItem(
                charset,
                isSelected = charset == currentCharset
            )
        }
    }

    val sourceTextWithNewEncoding: Flow<String> = currentCharset.map {
        fileImporterState.files[0].text
    }
}