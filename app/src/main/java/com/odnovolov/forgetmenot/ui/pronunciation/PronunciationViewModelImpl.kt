package com.odnovolov.forgetmenot.ui.pronunciation

import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryOwner
import com.odnovolov.forgetmenot.common.LiveEvent
import com.odnovolov.forgetmenot.entity.Pronunciation
import com.odnovolov.forgetmenot.entity.Speaker
import com.odnovolov.forgetmenot.ui.pronunciation.PronunciationViewModel.*
import com.odnovolov.forgetmenot.ui.pronunciation.PronunciationViewModel.Action.*
import com.odnovolov.forgetmenot.ui.pronunciation.PronunciationViewModel.Event.*
import com.odnovolov.forgetmenot.ui.pronunciation.PronunciationViewModel.State.DropdownLanguage
import java.util.*

class PronunciationViewModelImpl(
    handle: SavedStateHandle,
    private val speaker: Speaker,
    initPronunciation: Pronunciation,
    val resultCallback: ResultCallback
) : ViewModel(), PronunciationViewModel {

    class Factory(
        owner: SavedStateRegistryOwner,
        private val speaker: Speaker,
        private val initPronunciation: Pronunciation,
        private val resultCallback: ResultCallback
    ) : AbstractSavedStateViewModelFactory(owner, null) {
        override fun <T : ViewModel?> create(
            key: String,
            modelClass: Class<T>,
            handle: SavedStateHandle
        ): T {
            @Suppress("UNCHECKED_CAST")
            return PronunciationViewModelImpl(handle, speaker, initPronunciation, resultCallback) as T
        }
    }

    private val pronunciation: MutableLiveData<Pronunciation> = handle.getLiveData("pronunciation", initPronunciation)

    private val name: LiveData<String> = Transformations.map(pronunciation) { it.name }
    private val selectedQuestionLanguage: LiveData<Locale?> = Transformations.map(pronunciation) { it.questionLanguage }
    private val dropdownQuestionLanguages = MediatorLiveData<List<DropdownLanguage>>().apply {
        fun updateValue() {
            val availableLanguages = speaker.availableLanguages.value ?: return
            val selectedLanguage = selectedQuestionLanguage.value
            val defaultDropdownLanguage = DropdownLanguage(null, isSelected = selectedLanguage == null)
            val otherDropdownLanguages = availableLanguages.map { availableLanguage: Locale ->
                val isSelected = availableLanguage == pronunciation.value!!.questionLanguage
                DropdownLanguage(availableLanguage, isSelected)
            }
            value = listOf(defaultDropdownLanguage) + otherDropdownLanguages
        }

        addSource(selectedQuestionLanguage) { updateValue() }
        addSource(speaker.availableLanguages) { updateValue() }
    }
    private val questionAutoSpeak: LiveData<Boolean> = Transformations.map(pronunciation) { it.questionAutoSpeak }
    private val selectedAnswerLanguage: LiveData<Locale?> = Transformations.map(pronunciation) { it.answerLanguage }
    private val dropdownAnswerLanguages = MediatorLiveData<List<DropdownLanguage>>().apply {
        fun updateValue() {
            val availableLanguages = speaker.availableLanguages.value ?: return
            val selectedLanguage = selectedAnswerLanguage.value
            val defaultDropdownLanguage = DropdownLanguage(null, isSelected = selectedLanguage == null)
            val otherDropdownLanguages = availableLanguages.map { availableLanguage: Locale ->
                val isSelected = availableLanguage == pronunciation.value!!.answerLanguage
                DropdownLanguage(availableLanguage, isSelected)
            }
            value = listOf(defaultDropdownLanguage) + otherDropdownLanguages
        }

        addSource(selectedAnswerLanguage) { updateValue() }
        addSource(speaker.availableLanguages) { updateValue() }
    }
    private val answerAutoSpeak: LiveData<Boolean> = Transformations.map(pronunciation) { it.answerAutoSpeak }

    override val state = State(
        name,
        selectedQuestionLanguage,
        dropdownQuestionLanguages,
        questionAutoSpeak,
        selectedAnswerLanguage,
        dropdownAnswerLanguages,
        answerAutoSpeak
    )

    private val actionSender = LiveEvent<Action>()
    override val action = actionSender

    override fun onEvent(event: Event) {
        when (event) {
            is NameInputChanged -> {
                pronunciation.value = pronunciation.value!!.copy(name = event.enteredName)
            }
            QuestionLanguageButtonClicked -> {
                actionSender.send(ShowQuestionDropdownList)
            }
            is QuestionLanguageSelected -> {
                pronunciation.value = pronunciation.value!!.copy(questionLanguage = event.language)
                actionSender.send(DismissQuestionDropdownList)
            }
            is QuestionAutoSpeakSwitchClicked -> {
                val questionAutoSpeak = pronunciation.value!!.questionAutoSpeak.not()
                pronunciation.value = pronunciation.value!!.copy(questionAutoSpeak = questionAutoSpeak)
            }
            AnswerLanguageButtonClicked -> {
                actionSender.send(ShowAnswerDropdownList)
            }
            is AnswerLanguageSelected -> {
                pronunciation.value = pronunciation.value!!.copy(answerLanguage = event.language)
                actionSender.send(DismissAnswerDropdownList)
            }
            is AnswerAutoSpeakSwitchClicked -> {
                val answerAutoSpeak = pronunciation.value!!.answerAutoSpeak.not()
                pronunciation.value = pronunciation.value!!.copy(answerAutoSpeak = answerAutoSpeak)
            }
            DoneFabClicked -> {
                if (pronunciation.value!!.name.isEmpty()) {
                    actionSender.send(SetNameErrorText("Name is empty"))
                } else {
                    resultCallback.setResult(pronunciation.value!!)
                    actionSender.send(NavigateUp)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        speaker.shutdown()
    }

}