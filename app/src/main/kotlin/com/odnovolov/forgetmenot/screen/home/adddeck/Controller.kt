package com.odnovolov.forgetmenot.screen.home.adddeck

import com.odnovolov.forgetmenot.common.base.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.stageAdapter
import com.odnovolov.forgetmenot.screen.home.adddeck.AddDeckEvent.*
import com.odnovolov.forgetmenot.screen.home.adddeck.AddDeckOrder.SetDialogText
import com.odnovolov.forgetmenot.screen.home.adddeck.AddDeckOrder.ShowErrorMessage
import com.odnovolov.forgetmenot.screen.home.adddeck.Stage.*

class AddDeckController : BaseController<AddDeckEvent, AddDeckOrder>() {
    private val queries = database.addDeckControllerQueries

    override fun handleEvent(event: AddDeckEvent) {
        return when (event) {
            is ContentReceived -> {
                launchParsing(event.inputStream)
                queries.setFileName(event.fileName)
                setStage(Parsing)
            }

            is ParsingFinishedWithSuccess -> {
                val cardPrototypes = event.cardPrototypes
                val fileName = queries.getFileName().executeAsOne().fileName
                when {
                    fileName.isNullOrEmpty() -> {
                        saveCardPrototypes(cardPrototypes)
                        setStage(WaitingForName)
                    }
                    isDeckNameOccupied(fileName) -> {
                        saveCardPrototypes(cardPrototypes)
                        setStage(WaitingForName)
                        issueOrder(SetDialogText(fileName))
                    }
                    else -> {
                        queries.addDeck(name = fileName)
                        val deckId = queries.getLastInsertId().executeAsOne()
                        cardPrototypes.forEach {
                            queries.addCard(deckId, it.ordinal, it.question, it.answer)
                        }
                        finish()
                    }
                }
            }

            is ParsingFinishedWithError -> {
                issueOrder(ShowErrorMessage(event.e.message))
                finish()
            }

            is DialogTextChanged -> {
                val typedText = event.text
                queries.setTypedText(typedText)
                val errorText = when {
                    typedText.isEmpty() -> "Name cannot be empty"
                    isDeckNameOccupied(typedText) -> "This name is occupied"
                    else -> null
                }
                queries.setErrorText(errorText)
            }

            PositiveDialogButtonClicked -> {
                val typedText = queries.getTypedText().executeAsOne()
                queries.addDeck(name = typedText)
                val deckId = queries.getLastInsertId().executeAsOne()
                queries.addCardsFromCardPrototypeTable(deckId)
                finish()
            }

            NegativeDialogButtonClicked -> {
                finish()
            }
        }
    }

    private fun setStage(stage: Stage) {
        queries.setStage(stageAdapter.encode(stage))
    }

    private fun saveCardPrototypes(cardPrototypes: List<CardPrototype>) {
        queries.dropTableCardPrototype()
        queries.createTableCardPrototype()
        cardPrototypes.forEach {
            queries.addCardPrototype(it)
        }
    }

    private fun isDeckNameOccupied(testedName: String): Boolean {
        return queries.isDeckNameOccupied(testedName).executeAsOne()
    }

    private fun finish() {
        queries.dropTableCardPrototype()
        setStage(Idle)
    }
}