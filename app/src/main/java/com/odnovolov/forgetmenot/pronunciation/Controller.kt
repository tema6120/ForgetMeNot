package com.odnovolov.forgetmenot.pronunciation

import com.odnovolov.forgetmenot.common.BaseController
import com.odnovolov.forgetmenot.common.database.database
import com.odnovolov.forgetmenot.common.database.listOfLocalesAdapter
import com.odnovolov.forgetmenot.pronunciation.PronunciationEvent.*

class PronunciationController : BaseController<PronunciationEvent, PronunciationOrder>() {
    val queries: PronunciationControllerQueries = database.pronunciationControllerQueries

    override fun handleEvent(event: PronunciationEvent) {
        return when (event) {
            SavePronunciationButtonClicked -> {
                // TODO
            }

            is AvailableLanguagesUpdated -> {
                val availableLanguages = listOfLocalesAdapter.encode(event.languages.toList())
                queries.setAvailableLanguages(availableLanguages)
            }

            is QuestionLanguageSelected -> {
                // TODO
            }

            QuestionAutoSpeakSwitchClicked -> {
                // TODO
            }

            is AnswerLanguageSelected -> {
                // TODO
            }

            AnswerAutoSpeakSwitchClicked -> {
                // TODO
            }
        }
    }

}