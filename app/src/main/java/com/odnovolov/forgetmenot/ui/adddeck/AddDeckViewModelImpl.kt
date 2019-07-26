package com.odnovolov.forgetmenot.ui.adddeck

import androidx.lifecycle.*
import com.odnovolov.forgetmenot.entity.Card
import com.odnovolov.forgetmenot.entity.Deck
import com.odnovolov.forgetmenot.common.LiveEvent
import com.odnovolov.forgetmenot.ui.adddeck.AddDeckViewModel.*
import com.odnovolov.forgetmenot.ui.adddeck.AddDeckViewModel.Action.*
import com.odnovolov.forgetmenot.ui.adddeck.AddDeckViewModel.Event.*
import com.odnovolov.forgetmenot.ui.adddeck.Parser.IllegalCardFormatException
import java.nio.charset.Charset

class AddDeckViewModelImpl(
    private val dao: AddDeckDao,
    handle: SavedStateHandle
) : AddDeckViewModel {

    private enum class Stage {
        Idle,
        Parsing,
        WaitingForName
    }

    private val stage = handle.getLiveData("stage", Stage.Idle)
    private val occupiedDeckNames: LiveData<List<String>> = dao.getAllDeckNames()
    private val heldCards = handle.getLiveData<List<Card>>("heldCards").apply {
        stage.observeForever { stage ->
            if (stage == Stage.Idle) {
                value = null
            }
        }
    }
    private val enteredText = MutableLiveData("")

    private val isProcessing: LiveData<Boolean> = Transformations.map(stage) { it == Stage.Parsing }
    private val isDialogVisible: LiveData<Boolean> = Transformations.map(stage) { it == Stage.WaitingForName }
    private val errorText = MediatorLiveData<String>().apply {
        addSource(enteredText) { updateErrorText() }
        addSource(occupiedDeckNames) { updateErrorText() }
    }
    private val isPositiveButtonEnabled: LiveData<Boolean> = Transformations.map(errorText) { it == null }

    private fun updateErrorText() {
        errorText.value = when {
            enteredText.value!!.isEmpty() -> "Name cannot be empty"
            occupiedDeckNames.value!!.any { it == enteredText.value } -> "This name is occupied"
            else -> null
        }
    }

    override val state = State(
        isProcessing,
        isDialogVisible,
        errorText,
        isPositiveButtonEnabled
    )

    private val actionSender = LiveEvent<Action>()
    override val action = actionSender

    override fun onEvent(event: Event) {
        when (event) {
            AddDeckRequested -> {
                actionSender.send(ShowFileChooser)
            }
            is ContentReceived -> {
                stage.value = Stage.Parsing
                val cards = try {
                    Parser.parse(event.inputStream, Charset.defaultCharset())
                } catch (e: IllegalCardFormatException) {
                    stage.value = Stage.Idle
                    actionSender.send(ShowToast(e.message))
                    return
                }
                val fileName = event.fileName
                when {
                    fileName.isNullOrEmpty() -> {
                        stage.value = Stage.WaitingForName
                        heldCards.value = cards
                    }
                    occupiedDeckNames.value!!.any { it == fileName } -> {
                        stage.value = Stage.WaitingForName
                        heldCards.value = cards
                        actionSender.send(SetDialogText(fileName))
                    }
                    else -> {
                        val deck = Deck(name = fileName, cards = cards)
                        insertDeck(deck)
                    }
                }
            }
            is DialogTextChanged -> {
                enteredText.value = event.text
            }
            PositiveDialogButtonClicked -> {
                val deck = Deck(
                    name = enteredText.value!!,
                    cards = heldCards.value!!
                )
                insertDeck(deck)
            }
            NegativeDialogButtonClicked -> {
                stage.value = Stage.Idle
            }
        }
    }

    private fun insertDeck(deck: Deck) {
        Thread { dao.insertDeck(deck) }.start()
        stage.value = Stage.Idle
    }

}