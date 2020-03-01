package com.odnovolov.forgetmenot.presentation.screen.repetition

import org.koin.core.KoinComponent

class RepetitionScopeCloser : KoinComponent {
    var isServiceAlive = false
        set(value) {
            field = value
            closeScopeIfNeed()
        }

    var isFragmentAlive = false
        set(value) {
            field = value
            closeScopeIfNeed()
        }

    private fun closeScopeIfNeed() {
        if (!isServiceAlive && !isFragmentAlive) {
            getKoin().getScope(REPETITION_SCOPE_ID).close()
        }
    }
}