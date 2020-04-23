package com.odnovolov.forgetmenot.persistence.longterm.fullscreenpreference

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.persistence.FullscreenPreferenceDb
import com.odnovolov.forgetmenot.persistence.toFullscreenPreference
import com.odnovolov.forgetmenot.presentation.common.LongTermStateProvider
import com.odnovolov.forgetmenot.presentation.common.entity.FullscreenPreference

class FullscreenPreferenceProvider(
    private val database: Database
) : LongTermStateProvider<FullscreenPreference> {
    override fun load(): FullscreenPreference {
        lateinit var fullscreenPreferenceDb: FullscreenPreferenceDb
        database.transaction {
            fullscreenPreferenceDb = database.fullscreenPreferenceQueries
                .selectAll()
                .executeAsOne()
        }
        return fullscreenPreferenceDb.toFullscreenPreference()
    }

}