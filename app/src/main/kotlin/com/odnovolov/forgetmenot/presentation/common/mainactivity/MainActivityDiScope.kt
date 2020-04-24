package com.odnovolov.forgetmenot.presentation.common.mainactivity

import com.odnovolov.forgetmenot.persistence.longterm.fullscreenpreference.FullscreenPreferenceProvider
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.common.entity.FullscreenPreference

class MainActivityDiScope {
    val fullScreenPreference: FullscreenPreference =
        FullscreenPreferenceProvider(AppDiScope.get().database).load()

    companion object : DiScopeManager<MainActivityDiScope>() {
        fun shareFullScreenPreference(): FullscreenPreference {
            return diScope?.fullScreenPreference ?: error("MainActivityDiScope is not opened")
        }

        override fun recreateDiScope() = MainActivityDiScope()
    }
}