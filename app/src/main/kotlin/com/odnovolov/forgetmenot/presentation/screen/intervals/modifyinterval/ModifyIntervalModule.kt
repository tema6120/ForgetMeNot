package com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval

import com.odnovolov.forgetmenot.persistence.shortterm.ModifyIntervalDialogStateProvider
import com.odnovolov.forgetmenot.presentation.screen.intervals.INTERVALS_SCOPE_ID
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/*val modifyIntervalModule = module {
    scope<ModifyIntervalViewModel> {
        scoped { ModifyIntervalDialogStateProvider() }
        scoped { get<ModifyIntervalDialogStateProvider>().load() }
        scoped {
            ModifyIntervalController(
                intervalsSettings = getScope(INTERVALS_SCOPE_ID).get(),
                modifyIntervalDialogState = get(),
                longTermStateSaver = get(),
                modifyIntervalsScreenStateProvider = get<ModifyIntervalDialogStateProvider>()
            )
        }
        viewModel { ModifyIntervalViewModel(modifyIntervalDialogState = get()) }
    }
}*/

const val MODIFY_INTERVAL_SCOPE_ID = "MODIFY_INTERVAL_SCOPE_ID"