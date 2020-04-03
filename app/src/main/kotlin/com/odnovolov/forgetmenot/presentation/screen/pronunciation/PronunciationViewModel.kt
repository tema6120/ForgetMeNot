package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.entity.Deck
import com.odnovolov.forgetmenot.domain.entity.ExercisePreference
import com.odnovolov.forgetmenot.domain.entity.Pronunciation
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import org.koin.java.KoinJavaComponent.getKoin
import java.util.*

class PronunciationViewModel(
    deckSettingsState: DeckSettings.State,
    speakerImpl: SpeakerImpl
) : ViewModel() {
    private val availableLanguages: Flow<Set<Locale>> = speakerImpl.state
        .flowOf(SpeakerImpl.State::availableLanguages)

    private val currentPronunciation: Flow<Pronunciation> = deckSettingsState.deck
        .flowOf(Deck::exercisePreference)
        .flatMapLatest { exercisePreference: ExercisePreference ->
            exercisePreference.flowOf(ExercisePreference::pronunciation)
        }
        .share()

    val selectedQuestionLanguage: Flow<Locale?> = currentPronunciation
        .flatMapLatest { currentPronunciation: Pronunciation ->
            currentPronunciation.flowOf(Pronunciation::questionLanguage)
        }

    val dropdownQuestionLanguages: Flow<List<DropdownLanguage>> = combine(
        availableLanguages,
        selectedQuestionLanguage
    ) { availableLanguages: Set<Locale>, selectedQuestionLanguage: Locale? ->
        val defaultLanguage = DropdownLanguage(
            language = null,
            isSelected = selectedQuestionLanguage == null
        )
        val concreteLanguages = availableLanguages
            .map { language: Locale ->
                DropdownLanguage(
                    language = language,
                    isSelected = selectedQuestionLanguage == language
                )
            }
        listOf(defaultLanguage) + concreteLanguages
    }

    val questionAutoSpeak: Flow<Boolean> = currentPronunciation
        .flatMapLatest { currentPronunciation: Pronunciation ->
            currentPronunciation.flowOf(Pronunciation::questionAutoSpeak)
        }

    val selectedAnswerLanguage: Flow<Locale?> = currentPronunciation
        .flatMapLatest { currentPronunciation: Pronunciation ->
            currentPronunciation.flowOf(Pronunciation::answerLanguage)
        }

    val dropdownAnswerLanguages: Flow<List<DropdownLanguage>> = combine(
        availableLanguages,
        selectedAnswerLanguage
    ) { availableLanguages: Set<Locale>, selectedAnswerLanguage: Locale? ->
        val defaultLanguage = DropdownLanguage(
            language = null,
            isSelected = selectedAnswerLanguage == null
        )
        val concreteLanguages = availableLanguages
            .map { language: Locale ->
                DropdownLanguage(
                    language = language,
                    isSelected = selectedAnswerLanguage == language
                )
            }
        listOf(defaultLanguage) + concreteLanguages
    }

    val answerAutoSpeak: Flow<Boolean> = currentPronunciation
        .flatMapLatest { currentPronunciation: Pronunciation ->
            currentPronunciation.flowOf(Pronunciation::answerAutoSpeak)
        }

    val doNotSpeakTextInBrackets: Flow<Boolean> = currentPronunciation
        .flatMapLatest { currentPronunciation: Pronunciation ->
            currentPronunciation.flowOf(Pronunciation::doNotSpeakTextInBrackets)
        }

    override fun onCleared() {
        getKoin().getScope(PRONUNCIATION_SCOPE_ID).close()
    }
}