package com.odnovolov.forgetmenot.persistence.longterm.pronunciationpreference

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.DbKeys
import com.odnovolov.forgetmenot.persistence.setOfLocalesAdapter
import com.odnovolov.forgetmenot.presentation.common.LongTermStateProvider
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationPreference
import java.util.*

class PronunciationPreferenceProvider(
    private val database: Database
) : LongTermStateProvider<PronunciationPreference> {
    override fun load(): PronunciationPreference {
        val favoriteLanguages: Set<Locale> = database.keyValueQueries
            .selectValue(DbKeys.FAVORITE_LANGUAGES)
            .executeAsOneOrNull()
            ?.value
            ?.let(setOfLocalesAdapter::decode)
            ?: emptySet()
        return PronunciationPreference(favoriteLanguages)
    }
}