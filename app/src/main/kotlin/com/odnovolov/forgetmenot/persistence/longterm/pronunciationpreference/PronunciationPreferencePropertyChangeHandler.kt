package com.odnovolov.forgetmenot.persistence.longterm.pronunciationpreference

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.persistence.DbKeys
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.odnovolov.forgetmenot.persistence.setOfLocalesAdapter
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationPreference
import java.util.*

class PronunciationPreferencePropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.keyValueQueries

    override fun handle(change: Change) {
        if (change !is PropertyValueChange) return
        when (change.property) {
            PronunciationPreference::favoriteLanguages -> {
                val favoriteLanguages = change.newValue as Set<Locale>
                queries.replace(
                    key = DbKeys.FAVORITE_LANGUAGES,
                    value = setOfLocalesAdapter.encode(favoriteLanguages)
                )
            }
        }
    }
}