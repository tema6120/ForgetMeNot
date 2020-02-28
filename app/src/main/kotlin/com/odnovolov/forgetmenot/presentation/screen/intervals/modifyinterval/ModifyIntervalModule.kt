package com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval

import com.odnovolov.forgetmenot.presentation.common.Store
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DECK_SETTINGS_SCOPED_ID
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val modifyIntervalModule = module {
    scope<ModifyIntervalViewModel> {
        scoped {
            get<Store>().loadModifyIntervalDialogState(
                deckSettingsState = getScope(DECK_SETTINGS_SCOPED_ID).get()
            )
        }
        scoped { ModifyIntervalController(modifyIntervalDialogState = get(), store = get()) }
        viewModel { ModifyIntervalViewModel(modifyIntervalDialogState = get()) }
    }
}

const val MODIFY_INTERVAL_SCOPE_ID = "MODIFY_INTERVAL_SCOPE_ID"