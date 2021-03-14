package com.odnovolov.forgetmenot.persistence.longterm.lastusedlanguages

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change
import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change.PropertyValueChange
import com.odnovolov.forgetmenot.persistence.DbKeys
import com.odnovolov.forgetmenot.persistence.localeAdapter
import com.odnovolov.forgetmenot.persistence.longterm.PropertyChangeHandler
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.LastUsedLanguages
import java.util.*

class LastUsedLanguagesPropertyChangeHandler(
    database: Database
) : PropertyChangeHandler {
    private val queries = database.keyValueQueries

    override fun handle(change: Change) {
        if (change !is PropertyValueChange) return
        when (change.property) {
            LastUsedLanguages::language1 -> {
                val language1 = change.newValue as Locale?
                language1 ?: return
                queries.replace(
                    key = DbKeys.LAST_USED_LANGUAGE_1,
                    value = localeAdapter.encode(language1)
                )
            }
            LastUsedLanguages::language2 -> {
                val language2 = change.newValue as Locale?
                language2 ?: return
                queries.replace(
                    key = DbKeys.LAST_USED_LANGUAGE_2,
                    value = localeAdapter.encode(language2)
                )
            }
        }
    }
}