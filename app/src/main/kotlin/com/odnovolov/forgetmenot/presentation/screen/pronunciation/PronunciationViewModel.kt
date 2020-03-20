package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import androidx.lifecycle.ViewModel
import com.odnovolov.forgetmenot.presentation.common.customview.PresetPopupCreator.Preset
import com.odnovolov.forgetmenot.presentation.common.entity.NamePresetDialogStatus
import com.odnovolov.forgetmenot.domain.architecturecomponents.share
import com.odnovolov.forgetmenot.domain.checkPronunciationName
import com.odnovolov.forgetmenot.domain.entity.*
import com.odnovolov.forgetmenot.domain.interactor.decksettings.DeckSettings
import com.odnovolov.forgetmenot.domain.isIndividual
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import kotlinx.coroutines.flow.*
import org.koin.core.KoinComponent
import java.util.*

class PronunciationViewModel(
    private val deckSettingsState: DeckSettings.State,
    private val pronunciationScreenState: PronunciationScreenState,
    private val speakerImpl: SpeakerImpl,
    private val globalState: GlobalState
) : ViewModel(), KoinComponent {

    private val availableLanguages: Flow<Set<Locale>> = speakerImpl.state
        .flowOf(SpeakerImpl.State::availableLanguages)

    private val currentPronunciation: Flow<Pronunciation> = deckSettingsState.deck
        .flowOf(Deck::exercisePreference)
        .flatMapLatest { exercisePreference: ExercisePreference ->
            exercisePreference.flowOf(ExercisePreference::pronunciation)
        }
        .share()

    val pronunciation: Flow<Pronunciation> = currentPronunciation.flatMapLatest { it.asFlow() }

    val isSavePronunciationButtonEnabled: Flow<Boolean> = pronunciation.map { it.isIndividual() }

    val availablePronunciations: Flow<List<Preset>> = combine(
        currentPronunciation,
        globalState.flowOf(GlobalState::sharedPronunciations)
    ) { currentPronunciation: Pronunciation,
        sharedPronunciations: List<Pronunciation>
        ->
        (sharedPronunciations + currentPronunciation + Pronunciation.Default)
            .distinctBy { it.id }
    }
        .flatMapLatest { pronunciations: List<Pronunciation> ->
            val pronunciationNameFlows: List<Flow<String>> = pronunciations
                .map { it.flowOf(Pronunciation::name) }
            combine(pronunciationNameFlows) {
                val currentPronunciation = deckSettingsState.deck.exercisePreference.pronunciation
                pronunciations
                    .map { pronunciation: Pronunciation ->
                        with(pronunciation) {
                            Preset(
                                id = id,
                                name = name,
                                isSelected = id == currentPronunciation.id
                            )
                        }
                    }
                    .sortedWith(compareBy({ it.name }, { it.id }))
            }
        }

    val isNamePresetDialogVisible: Flow<Boolean> = pronunciationScreenState
        .flowOf(PronunciationScreenState::namePresetDialogStatus)
        .map { it != NamePresetDialogStatus.Invisible }

    val namePresetInputCheckResult: Flow<NameCheckResult> =
        pronunciationScreenState.flowOf(PronunciationScreenState::typedPresetName)
            .map { typedPresetName: String ->
                checkPronunciationName(typedPresetName, globalState)
            }

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