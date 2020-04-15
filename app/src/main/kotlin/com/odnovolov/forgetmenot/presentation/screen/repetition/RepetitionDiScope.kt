package com.odnovolov.forgetmenot.presentation.screen.repetition

import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.persistence.shortterm.RepetitionStateProvider
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import com.odnovolov.forgetmenot.presentation.common.di.AppDiScope
import com.odnovolov.forgetmenot.presentation.common.di.DiScopeManager
import com.odnovolov.forgetmenot.presentation.screen.repetition.service.RepetitionServiceController
import com.odnovolov.forgetmenot.presentation.screen.repetition.service.RepetitionServiceModel
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.RepetitionCardAdapter
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.RepetitionViewController
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.RepetitionViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel

class RepetitionDiScope private constructor(
    initialRepetitionState: Repetition.State? = null
) {
    private val repetitionStateProvider = RepetitionStateProvider(
        AppDiScope.get().json,
        AppDiScope.get().database,
        AppDiScope.get().globalState
    )

    private val repetitionState: Repetition.State =
        initialRepetitionState ?: repetitionStateProvider.load()

    private val speakerImpl = SpeakerImpl(
        AppDiScope.get().app
    )

    private val repetition = Repetition(
        repetitionState,
        speakerImpl,
        coroutineContext = Job() + businessLogicThread
    )

    val serviceController = RepetitionServiceController(
        repetition,
        AppDiScope.get().longTermStateSaver,
        repetitionStateProvider
    )

    val serviceModel = RepetitionServiceModel(
        repetitionState
    )

    val viewController = RepetitionViewController(
        repetition,
        AppDiScope.get().longTermStateSaver,
        repetitionStateProvider
    )

    val viewModel = RepetitionViewModel(
        repetitionState
    )

    val adapter = RepetitionCardAdapter(
        viewController
    )

    companion object : DiScopeManager<RepetitionDiScope>() {
        var isServiceAlive = false
            set(value) {
                field = value
                updateState()
            }

        var isFragmentAlive = false
            set(value) {
                field = value
                updateState()
            }

        private fun updateState() {
            when {
                isServiceAlive || isFragmentAlive -> reopenIfClosed()
                !isServiceAlive && !isFragmentAlive -> close()
            }
        }

        fun create(initialRepetitionState: Repetition.State) =
            RepetitionDiScope(initialRepetitionState)

        override fun recreateDiScope() = RepetitionDiScope()

        override fun onCloseDiScope(diScope: RepetitionDiScope) {
            with(diScope) {
                speakerImpl.shutdown()
                repetition.cancel()
                serviceController.dispose()
                viewController.dispose()
            }
        }
    }
}