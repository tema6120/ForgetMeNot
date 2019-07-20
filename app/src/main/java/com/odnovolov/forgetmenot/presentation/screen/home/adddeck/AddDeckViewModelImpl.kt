package com.odnovolov.forgetmenot.presentation.screen.home.adddeck

import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.feature.adddeck.IllegalCardFormatException
import com.odnovolov.forgetmenot.domain.feature.adddeck.Parser
import com.odnovolov.forgetmenot.presentation.common.ActionSender
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckViewModel.*
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckViewModel.Action.*
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckViewModel.Event.*
import java.nio.charset.Charset

class AddDeckViewModelImpl(
    private val dao: AddDeckDao,
    handle: SavedStateHandle
) : ViewModel(), AddDeckViewModel {

    class Factory(
        owner: SavedStateRegistryOwner,
        val dao: AddDeckDao
    ) : AbstractSavedStateViewModelFactory(owner, null) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            return AddDeckViewModelImpl(dao, handle) as T
        }
    }

    private enum class Stage {
        Idle, Parsing, WaitingForName
    }

    private val stage = handle.getLiveData("stage", Stage.Idle)
    private val occupiedDeckNames: LiveData<List<String>> = dao.getAllDeckNames()
    private val heldCards = handle.getLiveData<List<Card>>("heldCards")
    init {
        stage.observeForever { stage ->
            if (stage == Stage.Idle) {
                heldCards.value = null
            }
        }
    }
    private val enteredText = MutableLiveData("")

    private val isProcessing: LiveData<Boolean> = Transformations.map(stage) { it == Stage.Parsing }
    private val isDialogVisible: LiveData<Boolean> = Transformations.map(stage) { it == Stage.WaitingForName }
    private val errorText = MediatorLiveData<String?>().apply {
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
        isProcessing = isProcessing,
        isDialogVisible = isDialogVisible,
        errorText = errorText,
        isPositiveButtonEnabled = isPositiveButtonEnabled
    )

    private val actionSender = ActionSender<Action>()
    override val action = actionSender.getAction()

    override fun onEvent(event: Event) {
        when (event) {
            AddButtonClicked -> {
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
                val deck = Deck(name = enteredText.value!!, cards = heldCards.value!!)
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