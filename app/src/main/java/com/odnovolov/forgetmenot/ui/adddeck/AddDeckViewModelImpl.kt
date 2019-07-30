package com.odnovolov.forgetmenot.ui.adddeck

import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.odnovolov.forgetmenot.common.LiveEvent
import com.odnovolov.forgetmenot.entity.Card
import com.odnovolov.forgetmenot.entity.Deck
import com.odnovolov.forgetmenot.ui.adddeck.AddDeckViewModel.*
import com.odnovolov.forgetmenot.ui.adddeck.AddDeckViewModel.Action.*
import com.odnovolov.forgetmenot.ui.adddeck.AddDeckViewModel.Event.*
import com.odnovolov.forgetmenot.ui.adddeck.Parser.IllegalCardFormatException
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.Charset

class AddDeckViewModelImpl(
    private val dao: AddDeckDao,
    handle: SavedStateHandle
) : ViewModel(), AddDeckViewModel {

    class Factory(
        owner: SavedStateRegistryOwner,
        private val dao: AddDeckDao
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
        Idle,
        Parsing,
        WaitingForName
    }

    private val stage = handle.getLiveData("stage", Stage.Idle)
    private val heldCards = handle.getLiveData<List<Card>>("heldCards").apply {
        stage.observeForever { stage ->
            if (stage == Stage.Idle) {
                value = null
            }
        }
    }
    private val enteredText = MutableLiveData("")
    private var checkDeckNameJob: Job? = null

    private val isProcessing: LiveData<Boolean> = Transformations.map(stage) { it == Stage.Parsing }
    private val isDialogVisible: LiveData<Boolean> = Transformations.map(stage) { it == Stage.WaitingForName }
    private val errorText = MediatorLiveData<String>().apply {
        addSource(enteredText) { enteredText ->
            isPositiveButtonEnabled.value = false
            val oldCheckDeckNameJob = checkDeckNameJob
            if (oldCheckDeckNameJob != null && !oldCheckDeckNameJob.isCompleted) {
                oldCheckDeckNameJob.cancel()
            }
            checkDeckNameJob = checkDeckName(enteredText)
        }
    }
    private val isPositiveButtonEnabled = MutableLiveData(false)

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
            AddDeckButtonClicked -> {
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
                viewModelScope.launch {
                    when {
                        fileName.isNullOrEmpty() -> {
                            stage.value = Stage.WaitingForName
                            heldCards.value = cards
                        }
                        isDeckNameOccupied(fileName) -> {
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
            }
            is DialogTextChanged -> {
                enteredText.value = event.text
            }
            PositiveDialogButtonClicked -> {
                val deck = Deck(
                    name = enteredText.value!!,
                    cards = heldCards.value!!
                )
                viewModelScope.launch {
                    insertDeck(deck)
                }
            }
            NegativeDialogButtonClicked -> {
                stage.value = Stage.Idle
            }
        }
    }

    private fun checkDeckName(deckName: String): Job = viewModelScope.launch {
        val errorTextValue = when {
            deckName.isEmpty() -> "Name cannot be empty"
            isDeckNameOccupied(deckName) -> "This name is occupied"
            else -> null
        }
        if (isActive) {
            isPositiveButtonEnabled.value = errorTextValue == null
            errorText.value = errorTextValue
        }
    }

    private suspend fun isDeckNameOccupied(deckName: String): Boolean {
        return withContext(IO) {
            dao.isDeckNameOccupied(deckName)
        }
    }

    private suspend fun insertDeck(deck: Deck) {
        withContext(IO) {
            dao.insertDeck(deck)
        }
        stage.value = Stage.Idle
    }

}