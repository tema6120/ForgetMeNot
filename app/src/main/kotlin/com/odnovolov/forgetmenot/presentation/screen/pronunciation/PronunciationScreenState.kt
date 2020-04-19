package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationScreenState.WhatIsPronounced.NOTHING

class PronunciationScreenState {
    @Volatile
    var whatIsPronounced: WhatIsPronounced = NOTHING

    enum class WhatIsPronounced {
        NOTHING,
        QUESTION,
        ANSWER
    }
}