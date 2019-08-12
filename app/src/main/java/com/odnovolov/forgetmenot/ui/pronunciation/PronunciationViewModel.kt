package com.odnovolov.forgetmenot.ui.pronunciation

import androidx.lifecycle.LiveData
import com.odnovolov.forgetmenot.common.ViewModel
import com.odnovolov.forgetmenot.ui.pronunciation.PronunciationViewModel.*
import java.util.*

interface PronunciationViewModel : ViewModel<State, Action, Event> {

    data class State(
        val name: LiveData<String>,
        val selectedQuestionLanguage: LiveData<Locale?>,
        val dropdownQuestionLanguages: LiveData<List<DropdownLanguage>>,
        val questionAutoSpeak: LiveData<Boolean>,
        val selectedAnswerLanguage: LiveData<Locale?>,
        val dropdownAnswerLanguages: LiveData<List<DropdownLanguage>>,
        val answerAutoSpeak: LiveData<Boolean>
    ) {
        data class DropdownLanguage(
            val locale: Locale?,
            val isSelected: Boolean
        )
    }

    sealed class Action {
        object ShowQuestionDropdownList : Action()
        object DismissQuestionDropdownList : Action()
        object ShowAnswerDropdownList : Action()
        object DismissAnswerDropdownList : Action()
        data class SetNameErrorText(val errorText: String) : Action()
        object NavigateUp : Action()
    }

    sealed class Event {
        data class NameInputChanged(val enteredName: String) : Event()
        object QuestionLanguageButtonClicked : Event()
        data class QuestionLanguageSelected(val language: Locale?) : Event()
        object QuestionAutoSpeakSwitchClicked : Event()
        object AnswerLanguageButtonClicked : Event()
        data class AnswerLanguageSelected(val language: Locale?) : Event()
        object AnswerAutoSpeakSwitchClicked : Event()
        object DoneFabClicked : Event()
    }

}