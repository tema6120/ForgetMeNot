package com.odnovolov.forgetmenot.persistence.longterm

import com.odnovolov.forgetmenot.domain.architecturecomponents.PropertyChangeRegistry.Change

interface PropertyChangeHandler {
    fun handle(change: Change)
}